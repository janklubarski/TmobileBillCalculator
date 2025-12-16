import com.phonecompany.billing.TelephoneBillCalculator;
import com.phonecompany.billing.TelephoneBillCalculatorImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelephoneBillCalculatorImplTest {

    private final TelephoneBillCalculator telephoneBillCalculator = new TelephoneBillCalculatorImpl();

    @Test
    void callValue_peak_singleNumber() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:01:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:01:00").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - a single number
        assertEquals(new BigDecimal("0"), total);
    }

    @Test
    void callValue_peak_twoNumbers_firstIgnored() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:01:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:01:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 10:00:00,15-12-2025 10:01:00").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 1 minute in peak hours
        assertEquals(new BigDecimal("1"), total);
    }

    @Test
    void callValue_peak_twoNumbers_secondIgnored_tieBreaker() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:01:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 09:00:00,15-12-2025 09:01:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 10:00:00,15-12-2025 10:01:00").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 2 minutes in peak hours
        assertEquals(new BigDecimal("3"), total);
    }

    @Test
    void callValue_peak_threeNumbers_secondIgnored_tieBreaker() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:03:01").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:01:55,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 09:00:00,15-12-2025 09:01:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 10:00:00,15-12-2025 10:01:00").append(System.lineSeparator())
                .append("420123456791,15-12-2025 10:00:00,15-12-2025 10:01:00").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number pays for 5 (3 min + 1 sec (4 min), 0 min + 5 sec (1 min)) minutes in peak hours, the second one is free (larger number)
        // the third one pays for 1 in peak hours
        assertEquals(new BigDecimal("6"), total);
    }

    @Test
    void callValue_offPeak_threeNumbers_first_ignored() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:04:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 11:00:00,15-12-2025 11:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 07:00:00,15-12-2025 07:01:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 19:00:00,15-12-2025 19:01:00").append(System.lineSeparator())
                .append("420123456791,15-12-2025 23:59:59,16-12-2025 00:00:30").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 1 minute off peak, the third pays for 1 minute (30 sec) off peak
        assertEquals(new BigDecimal("1.50"), total);
    }

    @Test
    void callValue_longCallOffPeak_threeNumbers_first_ignored() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:04:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 11:00:00,15-12-2025 11:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 07:59:59,15-12-2025 08:00:49").append(System.lineSeparator())
                .append("420123456791,15-12-2025 19:00:00,15-12-2025 19:14:12").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 1 minute (50 sec) off peak while extending a few seconds into peak,
        //the third pays for 5 minutes off peak + 10 minutes (9 min + 12 sec) for long call
        assertEquals(new BigDecimal("5.00"), total);
    }

    @Test
    void callValue_longCallOnPeak_threeNumbers_first_ignored() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:04:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 11:00:00,15-12-2025 11:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 07:59:59,15-12-2025 08:00:49").append(System.lineSeparator())
                .append("420123456791,15-12-2025 15:00:00,15-12-2025 15:15:21").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 1 minute (50 sec) off peak while extending a few seconds into peak,
        //the third pays for 5 minutes on peak + 11 minutes (10 min + 21 sec) for long call
        assertEquals(new BigDecimal("7.70"), total);
    }

    @Test
    void callValue_shortCallExtendPeak_threeNumbers_first_ignored() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:04:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 11:00:00,15-12-2025 11:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 07:59:59,15-12-2025 08:02:49").append(System.lineSeparator())
                .append("420123456791,15-12-2025 15:00:00,15-12-2025 15:05:00").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 1 minute off peak while extending 2 minutes ( 1 min 50 sec) into peak,
        //the third pays for 5 minutes on peak
        assertEquals(new BigDecimal("7.50"), total);
    }

    @Test
    void callValue_shortCallExtendOffPeak_threeNumbers_first_ignored() {
        //given
        String phoneLog = new StringBuffer()
                .append("420123456789,15-12-2025 09:00:00,15-12-2025 09:04:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 10:00:00,15-12-2025 10:02:00").append(System.lineSeparator())
                .append("420123456789,15-12-2025 11:00:00,15-12-2025 11:02:00").append(System.lineSeparator())
                .append("420123456790,15-12-2025 15:59:59,15-12-2025 16:02:49").append(System.lineSeparator())
                .append("420123456791,15-12-2025 15:00:00,15-12-2025 15:05:00").append(System.lineSeparator())
                .toString();
        //when
        BigDecimal total = telephoneBillCalculator.calculate(phoneLog);
        //then - the first number is free, the second pays for 1 minute on peak while extending 2 minutes ( 1 min 50 sec) into peak,
        //the third pays for 5 minutes on peak
        assertEquals(new BigDecimal("7.00"), total);
    }

}
