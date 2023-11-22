package nav.portal.core.openingHours;


public class OpeningHoursDisplayData {
    private String ruleName; // Sett denne
    private String rule; // Sett denne rule for datoen
    private String openingHours; // eks 07-16
    private String displayText; // Det er ikke sikkert denne kan settes her // Stengt 7-16 , Lørdagsåpen

    OpeningHoursDisplayData(){

    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
