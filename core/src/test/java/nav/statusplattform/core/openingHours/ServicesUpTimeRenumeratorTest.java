package nav.statusplattform.core.openingHours;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;


public class ServicesUpTimeRenumeratorTest {

    private LocalDate nullDateEntry = null;

    private LocalDate dateGreaterThaCurrentDate = LocalDate.of(2025, 3, 1);

    private LocalDate normalFridayStartOfMonth = LocalDate.of(2024, 3, 1);
    private LocalDate thirdOfMarch = LocalDate.of(2024, 3, 3);
    private LocalDate normalFridayEndOfMonth = LocalDate.of(2024, 3, 30);
    private LocalDate weekendEndOfMonth = LocalDate.of(2024, 3, 31);

    private LocalDate todaysDate = LocalDate.now();
    private LocalDate yesterdayDate = todaysDate.minusDays(1);

    private LocalDate fiveDaysBack = todaysDate.minusDays(5);

    private String ohNull = null;
    private String ohAlwaysDown = "00:00-00:00";
    private String ohSevenToFive = "07:00-17:00";

    private LocalDate minusTwoMonths = yesterdayDate.minusMonths(2);

    private LocalDate minusOneYear = yesterdayDate.minusYears(1);


    @Test
    void getTotalOpeningHoursMinutes() {


//        int incorrectDateEntry = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(nullDateEntry, normalFridayEndOfMonth, ohSevenToFive);//incorrectDateEntry
//        int entryDateGreaterThankCurrentDate = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, dateGreaterThaCurrentDate, ohSevenToFive);//dateGreaterThanCurrentDate
//        int ohEqualsNull = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, normalFridayEndOfMonth, ohNull);//opening hours is equal to null
//        int serviceAlwaysDown = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, normalFridayEndOfMonth, ohAlwaysDown);//service always down

          /*int currentDateMinutes = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(todaysDate, todaysDate, ohSevenToFive);
          System.out.println("currentDateMinutes: " + currentDateMinutes);*/

        /*int yesterdaytoCurrentDay = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(yesterdayDate, todaysDate, ohSevenToFive);
        System.out.println("yesterdaytoCurrentDay: " + yesterdaytoCurrentDay);*/

        /*int fiveDaysBacktoCurrentDay = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(fiveDaysBack, todaysDate, ohSevenToFive);
        System.out.println("fiveDaysBacktoCurrentDay: " + fiveDaysBacktoCurrentDay);*/

        /*int fiveDaysBacktoCurrentDay = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(fiveDaysBack, todaysDate, ohSevenToFive);
        System.out.println("fiveDaysBacktoCurrentDay: " + fiveDaysBacktoCurrentDay);*/

        /*int twoMonthsBack = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(minusTwoMonths, yesterdayDate, ohSevenToFive);
        System.out.println("twoMonthsBack: " + twoMonthsBack);*/

        int oneYearBack = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(minusOneYear, yesterdayDate, ohSevenToFive);
        System.out.println("oneYearBack: " + oneYearBack);




        /*int MarchTotal0130 = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, normalFridayEndOfMonth, ohSevenToFive);
        System.out.println("MarchTotal0130: " + MarchTotal0130);
        int MarchTotal0131 = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, weekendEndOfMonth, ohSevenToFive);
        System.out.println("MarchTotal0131: " + MarchTotal0131);
        int week9StartOfMarch = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, thirdOfMarch, ohSevenToFive);
        System.out.println("week9StartOfMarch: " + week9StartOfMarch);*/

    }

}
