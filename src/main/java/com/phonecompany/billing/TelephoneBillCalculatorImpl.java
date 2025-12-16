package com.phonecompany.billing;

import com.phonecompany.billing.domain.TelephoneCall;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    //For most of these values, I'd recommend using application.yml and loading them through @Value -> but for the purposes of this task, this will do
    //Splitter for phone logs
    private static final String SPLITTER = ",";
    //DateTime formatter for input
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    //DateTime formatter for peak hours
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    //Peak hour start
    private static final LocalTime START_INTERVAL_PEAK = LocalTime.parse("08:00:00");
    //Peak hour end
    private static final LocalTime END_INTERVAL_PEAK = LocalTime.parse("16:00:00");
    //Standard rate
    private static final BigDecimal STANDARD_RATE = new BigDecimal("0.50");
    //Peak hour rate
    private static final BigDecimal PEAK_RATE = BigDecimal.ONE;
    //Long call length
    private static final Integer LONG_CALL_LENGTH = 5;
    //Long call rate
    private static final BigDecimal LONG_CALL_RATE = new BigDecimal("0.20");

    //I would validate the input as well, but for time constraints, I've decided to give it a pass and instead focus on unit tests + timely submit
    @Override
    public BigDecimal calculate(String phoneLog) {
        //first we have to get a list of all the incoming calls
        List<TelephoneCall> telephoneCalls = phoneLog.lines()
                .filter(line -> !line.isBlank())
                .map(this::parseCallLine)
                .toList();

        //then we find the promo number
        Long promoNumber = findPromoNumber(telephoneCalls);

        //now we process the billing of each call and return the total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (TelephoneCall call : telephoneCalls) {
            totalPrice = totalPrice.add(calculateCall(call, promoNumber));
        }
        return totalPrice;
    }

    private TelephoneCall parseCallLine(String callLine) {
        String[] split = callLine.split(SPLITTER);
        return TelephoneCall.builder()
                .telephoneNumber(Long.parseLong(split[0].trim()))
                .callStart(LocalDateTime.parse(split[1].trim(), TIMESTAMP_FORMATTER))
                .callEnd(LocalDateTime.parse(split[2].trim(), TIMESTAMP_FORMATTER))
                .build();
        //we could theoretically calculate the TelephoneNumberAnalytics per number during this cycle,
        //but for the purpose of this task, this would make the code less comprehensible -> but if there was a performance need, I'd do it
    }

    private Long findPromoNumber(List<TelephoneCall> calls) {
        //first, let's go through the list and map occurrences to each of the call
        //the tie breaker is like this:
        //1: number of calls
        //2: the larger number
        //therefore, we skip the total call duration (which could be another tiebreaker)
        HashMap<Long, Integer> callsPerNumberMap = new HashMap<>();
        Long promoNumber = 0l;
        Integer maxNumberOfCallsSoFar = 0;
        for (TelephoneCall call: calls) {
            Long number = call.getTelephoneNumber();
            Integer callsForNumberBefore = callsPerNumberMap.get(number);
            //increment the value of this number
            Integer callsForNumber = callsForNumberBefore == null ? 1 : callsForNumberBefore + 1;
            //update the map
            callsPerNumberMap.put(number, callsForNumber);

            //now we check for the promo number
            //the most important factor is the number of calls made
            if (callsForNumber > maxNumberOfCallsSoFar) {
                promoNumber = number;
                maxNumberOfCallsSoFar = callsForNumber;
            //if this is the first number or if several numbers match, we go with the "larger" telephone number
            } else if (promoNumber == null || (callsForNumber == maxNumberOfCallsSoFar && number > promoNumber)) {
                promoNumber = number;
            }
        }
        return promoNumber;
    }

    private BigDecimal calculateCall(TelephoneCall call, Long promoNumber) {
        //promo numbers are not to be calculated
        if (Objects.equals(promoNumber, call.getTelephoneNumber())) {
            return BigDecimal.ZERO;
        }
        //else we calculate the price based on time and the call length
        LocalDateTime start = call.getCallStart();
        LocalDateTime end = call.getCallEnd();
        long durationInSeconds = Duration.between(start, end).getSeconds();
        //number of started minutes = ceil(seconds / 60) -> 300 seconds = 5 started minutes, 301 seconds = 6 started minutes
        long billedMinutes = (durationInSeconds + 59) / 60;
        BigDecimal price = BigDecimal.ZERO;

        //now we calculate the price, minute by minute -> first minute starts with an index 0
        for (int minuteIndex = 0; minuteIndex < billedMinutes; minuteIndex++) {
            BigDecimal rate;
            //if a call is longer than LONG_CALL_LENGTH (5 minutes by default), every started minute after that period will cost the LONG_CALL_RATE
            //billing price (0.20 CZK by default)
            if (billedMinutes > LONG_CALL_LENGTH && minuteIndex >= LONG_CALL_LENGTH) {
                rate = LONG_CALL_RATE;
            //otherwise, we check whether the started minute falls into the peak hours (START_INTERVAL_PEAK / END_INTERVAL_PEAK) ->
            //that is, 08:00:00 and 16:00:00 by default
            } else {
                LocalTime minuteStartTime = start.plusMinutes(minuteIndex).toLocalTime();
                boolean isPeak = !minuteStartTime.isBefore(START_INTERVAL_PEAK) && minuteStartTime.isBefore(END_INTERVAL_PEAK);
                rate = isPeak ? PEAK_RATE : STANDARD_RATE;
            }
            price = price.add(rate);
        }
        return price;
    }

}
