package no.nav.portal.rest.api.wcag;

import java.util.HashMap;

public class CriteriaMap {

    static HashMap<String,String> readAbleMap = new HashMap<>();
    static {
        readAbleMap.put("WCAG21:non-text-content","1.1.1: Non-text Content");

        readAbleMap.put("WCAG21:audio-only-and-video-only-prerecorded","1.2.1: Audio-only and Video-only (Prerecorded)");
        readAbleMap.put("WCAG21:captions-prerecorded","1.2.2: Captions (Prerecorded)");
        readAbleMap.put("WCAG21:audio-description-or-media-alternative-prerecorded","1.2.3: Audio Description or Media Alternative (Prerecorded)");
        readAbleMap.put("WCAG21:captions-live","1.2.4: Captions (Live)");
        readAbleMap.put("WCAG21:audio-description-prerecorded","1.2.5: Audio Description (Prerecorded)");
        readAbleMap.put("WCAG21:sign-language-prerecorded","1.2.6: Sign Language (Prerecorded)");
        readAbleMap.put("WCAG21:extended-audio-description-prerecorded","1.2.7: Extended Audio Description (Prerecorded)");
        readAbleMap.put("WCAG21:media-alternative-prerecorded","1.2.8: Media Alternative (Prerecorded)");
        readAbleMap.put("WCAG21:audio-only-live","1.2.9: Audio-only (Live)");

        readAbleMap.put("WCAG21:info-and-relationships","1.3.1: Info and Relationships");
        readAbleMap.put("WCAG21:meaningful-sequence","1.3.2: Meaningful Sequence");
        readAbleMap.put("WCAG21:sensory-characteristics","1.3.3: Sensory Characteristics");
        readAbleMap.put("WCAG21:orientation","1.3.4: Orientation");
        readAbleMap.put("WCAG21:identify-input-purpose","1.3.5: Identify Input Purpose");
        readAbleMap.put("WCAG21:identify-purpose","1.3.6: Identify Purpose");

        readAbleMap.put("WCAG21:use-of-color","1.4.1: Use of Color");
        readAbleMap.put("WCAG21:audio-control","1.4.2: Audio Control");
        readAbleMap.put("WCAG21:contrast-minimum","1.4.3: Contrast (Minimum)");
        readAbleMap.put("WCAG21:resize-text","1.4.4: Resize text");
        readAbleMap.put("WCAG21:images-of-text","1.4.5: Images of Text");
        readAbleMap.put("WCAG21:contrast-enhanced","1.4.6: Contrast (Enhanced)");
        readAbleMap.put("WCAG21:low-or-no-background-audio","1.4.7: Low or No Background Audio");
        readAbleMap.put("WCAG21:visual-presentation","1.4.8: Visual Presentation");
        readAbleMap.put("WCAG21:images-of-text-no-exception","1.4.9: Images of Text (No Exception)");
        readAbleMap.put("WCAG21:reflow","1.4.10: Reflow");
        readAbleMap.put("WCAG21:non-text-contrast","1.4.11: Non-text Contrast");
        readAbleMap.put("WCAG21:text-spacing","1.4.12: Text Spacing");
        readAbleMap.put("WCAG21:content-on-hover-or-focus","1.4.13: Content on Hover or Focus");

        readAbleMap.put("WCAG21:keyboard","2.1.1: Keyboard");
        readAbleMap.put("WCAG21:no-keyboard-trap","2.1.2: No Keyboard Trap");
        readAbleMap.put("WCAG21:keyboard-no-exception","2.1.3: Keyboard (No Exception)");
        readAbleMap.put("WCAG21:character-key-shortcuts","2.1.4: Character Key Shortcuts");

        readAbleMap.put("WCAG21:timing-adjustable","2.2.1: Timing Adjustable");
        readAbleMap.put("WCAG21:pause-stop-hide","2.2.2: Pause, Stop, Hide");
        readAbleMap.put("WCAG21:no-timing","2.2.3: No Timing");
        readAbleMap.put("WCAG21:interruptions","2.2.4: Interruptions");
        readAbleMap.put("WCAG21:re-authenticating","2.2.5: Re-authenticating");
        readAbleMap.put("WCAG21:timeouts","2.2.6: Timeouts");

        readAbleMap.put("WCAG21:three-flashes-or-below-threshold","2.3.1: Three Flashes or Below Threshold");
        readAbleMap.put("WCAG21:three-flashes","2.3.2: Three Flashes");
        readAbleMap.put("WCAG21:animation-from-interactions","2.3.3: Animation from Interactions");

        readAbleMap.put("WCAG21:bypass-blocks","2.4.1: Bypass Blocks");
        readAbleMap.put("WCAG21:page-titled","2.4.2: Page Titled");
        readAbleMap.put("WCAG21:focus-order","2.4.3: Focus Order");
        readAbleMap.put("WCAG21:link-purpose-in-context","2.4.4: Link Purpose (In Context)");
        readAbleMap.put("WCAG21:multiple-ways","2.4.5: Multiple Ways");
        readAbleMap.put("WCAG21:headings-and-labels","2.4.6: Headings and Labels");
        readAbleMap.put("WCAG21:focus-visible","2.4.7: Focus Visible");
        readAbleMap.put("WCAG21:location","2.4.8: Location");
        readAbleMap.put("WCAG21:link-purpose-link-only","2.4.9: Link Purpose (Link Only)");
        readAbleMap.put("WCAG21:section-headings","2.4.10: Section Headings");

        readAbleMap.put("WCAG21:pointer-gestures","2.5.1: Pointer Gestures");
        readAbleMap.put("WCAG21:pointer-cancellation","2.5.2: Pointer Cancellation");
        readAbleMap.put("WCAG21:label-in-name","2.5.3: Label in Name");
        readAbleMap.put("WCAG21:motion-actuation","2.5.4: Motion Actuation");
        readAbleMap.put("WCAG21:target-size","2.5.5: Target Size");
        readAbleMap.put("WCAG21:concurrent-input-mechanisms","2.5.6: Concurrent Input Mechanisms");

        readAbleMap.put("WCAG21:language-of-page","3.1.1: Language of Page");
        readAbleMap.put("WCAG21:language-of-parts","3.1.2: Language of Parts");
        readAbleMap.put("WCAG21:unusual-words","3.1.3: Unusual Words");
        readAbleMap.put("WCAG21:abbreviations","3.1.4: Abbreviations");
        readAbleMap.put("WCAG21:reading-level","3.1.5: Reading Level");
        readAbleMap.put("WCAG21:pronunciation","3.1.6: Pronunciation");

        readAbleMap.put("WCAG21:on-focus","3.2.1: On Focus");
        readAbleMap.put("WCAG21:on-input","3.2.2: On Input");
        readAbleMap.put("WCAG21:consistent-navigation","3.2.3: Consistent Navigation");
        readAbleMap.put("WCAG21:consistent-identification","3.2.4: Consistent Identification");
        readAbleMap.put("WCAG21:change-on-request","3.2.5: Change on Request");


        readAbleMap.put("WCAG21:error-identification","3.3.1: Error Identification");
        readAbleMap.put("WCAG21:labels-or-instructions","3.3.2: Labels or Instructions");
        readAbleMap.put("WCAG21:error-suggestion","3.3.3: Error Suggestion");
        readAbleMap.put("WCAG21:error-prevention-legal-financial-data","3.3.4: Error Prevention (Legal, Financial, Data)");
        readAbleMap.put("WCAG21:help","3.3.5: Help");
        readAbleMap.put("WCAG21:error-prevention-all","3.3.6: Error Prevention (All)");

        readAbleMap.put("WCAG21:parsing","4.1.1: Parsing");
        readAbleMap.put("WCAG21:name-role-value","4.1.2: Name, Role, Value");
        readAbleMap.put("WCAG21:status-messages","4.1.3: Status Messages");
    }
    public static String mapToReadAble(String criteriaId){
        return readAbleMap.getOrDefault(criteriaId, criteriaId);
    }
}
