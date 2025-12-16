## Zadání
Vaším úkolem je implementace technického Maven nebo Gradle modulu pro výpočet částky k úhradě za telefonní účet dle výpisu volání. Na vypracování máte cca 3-4h.
Modul by měl implementovat následující rozhraní:

```
package com.phonecompany.billing;

import java.math.BigDecimal;

public interface TelephoneBillCalculator {

   BigDecimal calculate (String phoneLog);

}
```

Vstupem metody je textový řetězec obsahující výpis volání. Výpis volání je ve formátu CSV s následujícími poli:
Telefonní číslo v normalizovaném tvaru obsahující pouze čísla (např. 420774567453)

Začátek hovoru ve tvaru dd-MM-yyyy HH:mm:ss

Konec hovoru ve tvaru dd-MM-yyyy HH:mm:ss
Příklad:
```
420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00
```
Pro účel testu můžete předpokládat, že data na vstupu budou vždy striktně splňovat výše uvedená pravidla.

Výstupem metody je částka k uhrazení spočtena dle vstupního výpisu dle následujících pravidel:

Minutová sazba v intervalu <8:00:00,16:00:00) je zpoplatněna 1 Kč za každou započatou minutu. Mimo uvedený interval platí snížená sazba 0,50 Kč za každou započatou minutu. Pro každou minutu hovoru je pro stanovení sazby určující čas započetí dané minuty.

Pro hovory delší, než pět minut platí pro každou další započatou minutu nad rámec prvních pěti minut snížená sazba 0,20 Kč bez ohledu na dobu kdy telefonní hovor probíhá.

V rámci promo akce operátora dále platí, že hovory na nejčastěji volané číslo v rámci výpisu nebudou zpoplatněny. V případě, že by výpis obsahoval dvě nebo více takových čísel, zpoplatněny nebudou hovory na číslo s aritmeticky nejvyšší hodnotou.

Výsledek vaší práce umístěte do repository ve některém z veřejných úložišť zdrojových kódů (např. GitHub) a zašlete nám na něj odkaz. Repository by mělo obsahovat veškeré zdrojové kódy a pomocné soubory pro sestavení technického modulu plus automatické testy.
