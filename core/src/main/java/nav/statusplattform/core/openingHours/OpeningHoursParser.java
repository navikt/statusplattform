package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursRule;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.enums.RuleType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class OpeningHoursParser {


    private static String RULE_NOT_APPLIES = "rule_not_applies";


    public static String getOpeninghours(LocalDate dateEntry, OpeningHoursGroup group) {
        return getOpeninghours(dateEntry,group.getRules(),false);
    }

    private static String getOpeninghours(LocalDate dateEntry, List<OpeningHoursRule> rules, Boolean isSubGroup){
        if(rules.size() == 0){
            if(isSubGroup){
                return RULE_NOT_APPLIES;
            }
            return "00:00-00:00";
        }
        OpeningHoursRule firstRGentry = rules.get(0);
        if(firstRGentry.getRuleType().equals(RuleType.RULE)){
            String firstruleOpeningHours = getOpeninghours(dateEntry,((OpeningHoursRuleEntity)firstRGentry).getRule());
            if(!firstruleOpeningHours.equals(RULE_NOT_APPLIES)){
                return firstruleOpeningHours;
            }
        }
        else {
            String firstruleOpeningHours = getOpeninghours(dateEntry,((OpeningHoursGroup) firstRGentry).getRules(),true);
            if(!firstruleOpeningHours.equals(RULE_NOT_APPLIES)){
                return firstruleOpeningHours;
            }
        }
        return getOpeninghours(dateEntry,rules.subList(1, rules.size()),isSubGroup);
    }

    public static OpeningHoursDisplayData getDisplayData(LocalDate dateEntry,OpeningHoursGroup group){
        return getDisplayData(dateEntry,group.getRules(),false);
    }

    private static OpeningHoursDisplayData getDisplayData(LocalDate dateEntry, List<OpeningHoursRule> rules, Boolean isSubGroup){
        //TODO FYLL INN HER ORLENE
        OpeningHoursDisplayData openingHoursDisplayData = new OpeningHoursDisplayData();
        if(rules.size() == 0){
            if(isSubGroup){
                openingHoursDisplayData.setRule(RULE_NOT_APPLIES);
                return openingHoursDisplayData;
            }
                openingHoursDisplayData.setRule("No Rules stated");
                openingHoursDisplayData.setOpeningHours("00:00-00:00");
                openingHoursDisplayData.setDisplayText("Åpen - ingen gjeldende dato regler");
                return openingHoursDisplayData;
        }

        OpeningHoursRule firstRGentry = rules.get(0);

        if(firstRGentry.getRuleType().equals(RuleType.RULE)){
            String firstruleOpeningHours = getOpeninghours(dateEntry,((OpeningHoursRuleEntity)firstRGentry).getRule());
            if(!firstruleOpeningHours.equals(RULE_NOT_APPLIES)){
                openingHoursDisplayData.setRuleName(rules.get(0).getName());
                openingHoursDisplayData.setRule(((OpeningHoursRuleEntity) firstRGentry).getRule());
                openingHoursDisplayData.setOpeningHours(firstruleOpeningHours);
                return openingHoursDisplayData;
            }
        }
        else {
            OpeningHoursDisplayData firstruleOpeningHours = getDisplayData(dateEntry,((OpeningHoursGroup) firstRGentry).getRules(),true);
            if(!firstruleOpeningHours.getRule().equals(RULE_NOT_APPLIES)){
                return firstruleOpeningHours;
            }
        }
        return  getDisplayData(dateEntry,rules.subList(1, rules.size()),isSubGroup);
    }


    public static String getOpeninghours(LocalDate dateEntry, String rule) {
//        "00:00-00:00"LocalDateTime
        LocalDateTime dateTimeEntry = LocalDateTime.of(dateEntry, LocalTime.of(0,0));
        String[] ruleParts = rule.split("[\s]");

        if (!isRuleApplicableForDate(dateTimeEntry, ruleParts[0])){
            return RULE_NOT_APPLIES;
        }

        if(!isRuleApplicableForDaysInMonth(dateTimeEntry, ruleParts[1])){
            return RULE_NOT_APPLIES;
        }

        if(!isRuleApplicableForWeekDays(dateTimeEntry, ruleParts[2])){
            return RULE_NOT_APPLIES;
        }

        return ruleParts[3];
    }

    public static boolean isOpen(LocalDateTime dateTimeEntry, String rule) {

        String[] ruleParts = rule.split("[\s]");

        if (!isRuleApplicableForDate(dateTimeEntry, ruleParts[0])){
            return false;
        }

        if(!isRuleApplicableForDaysInMonth(dateTimeEntry, ruleParts[1])){
            return false;
        }

        if(!isRuleApplicableForWeekDays(dateTimeEntry, ruleParts[2])){
            return false;
        }

        return isRuleApplicableForOpeningTimes(dateTimeEntry ,ruleParts[3]);
    }

    private static boolean isRuleApplicableForDate(LocalDateTime dateTimeEntry, String dateRule) {
        //Checks for ??.??.????

        if (dateRule.equals("??.??.????")) {
            return true;
        }

        String[] ruleParts = dateRule.split("[.]"); //Rule's date
        //Checks formats ??.mm.???? and dd.mm.????
        if (ruleParts[2].equals("????")) {
            if (ruleParts[0].equals("??")) {
                //Checks the date entry and rule date for a match of the month
                String mmdateEntry = String.format("%02d",dateTimeEntry.getMonthValue());
                return ruleParts[1].equals(mmdateEntry);
            }
            //Checks the date entry and rule date for a dd.mm match
            String ddmmRule = dateRule.substring(0,5);
            String ddmmdateEntry = String.format("%02d",dateTimeEntry.getDayOfMonth()) +"."+ String.format("%02d",dateTimeEntry.getMonthValue());
            return ddmmRule.equals(ddmmdateEntry);
        }
        LocalDate ruleDate = LocalDate.of(Integer.parseInt(ruleParts[2]),Integer.parseInt(ruleParts[1]),Integer.parseInt(ruleParts[0]));
        LocalDate dateEntry = dateTimeEntry.toLocalDate();
        //Checks the date entry and rule date for a match of the dd.mm.yyyy
        return dateEntry.equals(ruleDate);
    }

    private static boolean isRuleApplicableForDaysInMonth(LocalDateTime dateTimeEntry, String dayInMonthRule) {
        //Checks for ?
        if (dayInMonthRule.equals("?")) {
            return true;
        }

        dayInMonthRule= dayInMonthRule.replaceAll("[L]",String.valueOf(dateTimeEntry.toLocalDate().lengthOfMonth()));
        String[] ruleParts = dayInMonthRule.split("[,]");

        int lowerRange;
        int upperRange;
        int dayOfTheMonth = dateTimeEntry.getDayOfMonth();
        for (String rulePart : ruleParts) {
            if (rulePart.contains("-")) {
                String[]  splittedRulePart = rulePart.split("[-]");
                lowerRange = Integer.parseInt(splittedRulePart[0]);
                upperRange= Integer.parseInt(splittedRulePart[1]);
                if (lowerRange <= dayOfTheMonth  && dayOfTheMonth <= upperRange)
                    return true;
            } else {
                //checks weekday value matches a single weekday value
                if (dayOfTheMonth == Integer.parseInt(rulePart)) {
                    return true;
                }
            }
        }
        System.out.println("Did not match month rule: "+ dayInMonthRule+ " date: "+ dateTimeEntry.toLocalDate().toString() );
        return false;
    }


    private static boolean isRuleApplicableForWeekDays(LocalDateTime dateTimeEntry, String weekDayRule){
        //Checks for ?
        if (weekDayRule.equals("?")) {
            return true;
        }

        int dayOfWeekNumber = dateTimeEntry.getDayOfWeek().getValue();

        //Checks for a singular day in the month or for last day in the month(L) entry
        String[] ruleParts = weekDayRule.split("[,]");

        int lowerRange;
        int upperRange;
        //check the range/s for a weekday number match
        for (String rulePart : ruleParts) {
            if (rulePart.contains("-")) {
                //checks weekday falls within a range
                lowerRange = Integer.parseInt(rulePart.substring(0, 1));
                upperRange = Integer.parseInt(rulePart.substring(2));
                if (lowerRange <= dayOfWeekNumber  && dayOfWeekNumber <= upperRange)
                    return true;
            } else {
                //checks weekday value matches a single weekday value
                if (dayOfWeekNumber == Integer.parseInt(rulePart)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isRuleApplicableForOpeningTimes(LocalDateTime dateTimeEntry, String timeRule){
        //checks for hh:mm-hh:mm
        if (timeRule.equals ("00:00-00:00")){
            System.out.println("always closed");
            return false;
        }

        if (timeRule.equals ("00:00-23:59")){
            System.out.println("open around the clock");
            return true;
        }

        String[] ruleParts = timeRule.split("[-]");

        //Obtain the time in hh:mm format
        LocalTime time = LocalTime.of(dateTimeEntry.getHour(),dateTimeEntry.getMinute());
        String[] openingString = ruleParts[0].split("[:]");
        String[] closingString = ruleParts[1].split("[:]");
        LocalTime opening = LocalTime.of(Integer.parseInt(openingString[0]),Integer.parseInt(openingString[1])).minusMinutes(1);
        LocalTime closing = LocalTime.of(Integer.parseInt(closingString[0]),Integer.parseInt(closingString[1])).plusMinutes(1);

        return time.isAfter(opening) && time.isBefore(closing);

    }

    public static LocalTime getOpeningTime(String timeRule) {
        String[] ruleParts = timeRule.split("[-]");
        //Obtain opening time in hh:mm format
        String[] openingString = ruleParts[0].split("[:]");
        return LocalTime.of(Integer.parseInt(openingString[0]), Integer.parseInt(openingString[1]));
    }

    public static LocalTime getClosingTime(String timeRule) {
        String[] ruleParts = timeRule.split("[-]");
        //Obtain opening time in hh:mm format
        String[] closingString = ruleParts[1].split("[:]");
        return LocalTime.of(Integer.parseInt(closingString[0]), Integer.parseInt(closingString[1]));
    }


}
