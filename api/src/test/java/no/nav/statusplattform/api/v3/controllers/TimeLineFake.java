package no.nav.statusplattform.api.v3.controllers;

import java.time.LocalDate;
import java.util.LinkedHashMap;

public record TimeLineFake(LinkedHashMap<LocalDate, TimeLineEntryFake> map) {

}
