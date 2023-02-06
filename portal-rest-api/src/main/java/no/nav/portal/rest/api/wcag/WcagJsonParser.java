package no.nav.portal.rest.api.wcag;

import no.nav.portal.infrastructure.AuthenticationFilter;
import no.portal.web.generated.api.KravMapEntryDto;
import no.portal.web.generated.api.WcagKravDto;
import no.portal.web.generated.api.WcagResultDto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WcagJsonParser {
    private static final Logger logger = LoggerFactory.getLogger(WcagJsonParser.class);


    public static List<String> getAllKravs(){
        List<WcagResultDto> resultDtos = readAllReports();
        ArrayList<String> alleKrav = new ArrayList<>();
        resultDtos.forEach(result -> {
            result.getKrav().forEach(krav -> {
                if(!alleKrav.contains(krav.getId())){
                    alleKrav.add(krav.getId());
                }
            });
        });
        return alleKrav;
    }

    public static HashMap<String, ArrayList<String>>  getAllKravsMap(){
        List<WcagResultDto> resultDtos = readAllReports();
        ArrayList<String> alleKrav = new ArrayList<>();
        resultDtos.forEach(result -> {
            result.getKrav().forEach(krav -> {
                if(!alleKrav.contains(krav.getId())){
                    alleKrav.add(krav.getId());
                }
            });
        });
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        alleKrav.forEach(kravId -> {
            result.put(kravId, new ArrayList<>());
            resultDtos.forEach(resultDto -> {
                List<WcagKravDto> matching = resultDto.getKrav().stream().filter(krav -> krav.getId().equals(kravId)).collect(Collectors.toList());
                if(matching.size()== 1) {
                   result.get(kravId).add(matching.get(0).getSubject());
                }
                if(matching.size() > 1){
                    matching.forEach(resultOfKrav -> {
                                    result.get(kravId).add(resultDto.getName() +", " + resultOfKrav.getSubject());

                            }

                    );
                }
            });

        });
        return result;
    }



    public static List<KravMapEntryDto> getAllKravsMapDto(){
        List<WcagResultDto> allReports = readAllReports();
        ArrayList<String> allCreterias = new ArrayList<>();
        allReports.forEach(report -> {
            report.getKrav().forEach(krav -> {
                if(!allCreterias.contains(krav.getId())){
                    allCreterias.add(krav.getId());
                }
            });
        });
        ArrayList<String> allCreteriasSorted = (ArrayList<String>) allCreterias.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        ArrayList<KravMapEntryDto> result = new ArrayList<>();
        allCreteriasSorted.forEach(kravId -> {
            ArrayList<WcagKravDto> listOfServicesWithCriteria = new ArrayList<>();
            allReports.forEach(resultDto -> {
                List<WcagKravDto> matching = resultDto.getKrav().stream().filter(krav -> krav.getId().equals(kravId)).collect(Collectors.toList());
                if(matching.size() > 0){
                    matching.forEach(resultOfKrav -> {
                                if (!resultOfKrav.getSubject().equals(resultDto.getName())) {
                                    resultOfKrav.setSubject(resultDto.getName() + ", " + resultOfKrav.getSubject());
                                }
                                listOfServicesWithCriteria.add(resultOfKrav);
                            });
                }
            });
            KravMapEntryDto dto = new KravMapEntryDto();
            dto.setNavn(kravId);
            dto.setSubject(listOfServicesWithCriteria);
            result.add(dto);

        });

        return result;
    }


    public static List<WcagResultDto> readAllReports(){
        List<String> allReportsAsString = readAllFiles();

        List<WcagResultDto> wcagResultDtos = allReportsAsString.stream()
                .map(WcagJsonParser::parseResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        wcagResultDtos.forEach(
                dto ->
                    dto.setKrav(dto.getKrav().stream().sorted(Comparator.comparing(WcagKravDto::getId)).collect(Collectors.toList()))

        );
        return wcagResultDtos;
    }

    public static List<String>  readAllFiles() {

        List<String> filesInFolder = null;
        String path = getPathToReports();

        try {
            filesInFolder = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .map(WcagJsonParser::readFileFromPath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesInFolder;
    }

    public static String readFileFromPath(Path filePath){

        try{
            String content = Files.readString(filePath);
            return content;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return "";
    }

    private static String getPathToReports(){
        String path = System.getProperty("user.dir");
        if(path.contains("adevguide")){
            path +="/rapporter";
            return path;
        }
        if (!path.contains("portal-rest-api")
        ) {
            path += "/portal-rest-api";
        }
        path+= "/src/main/resources/rapporter";
        logger.info(path);
        return path;
    }


    public static WcagResultDto parseResult(String input){
        WcagResultDto wcagResultDto = new WcagResultDto();
        JSONObject object;
        try{
            object = readJson(input);
        }
        catch (Exception e){
            return null;
        }
        String name = getNameOfServiceTested(object);
        wcagResultDto.setName(name);
        List<WcagKravDto> kravArrayList = WcagJsonParser.getKrav(object);
        wcagResultDto.setKrav(kravArrayList);
        String summary = getSummary(object);
        wcagResultDto.setSummary(summary);
        return wcagResultDto;
    }



    public static JSONObject readJson(String jsonString){
        return new JSONObject(jsonString);
    }


    public static String getNameOfServiceTested(JSONObject object){
        try{
            JSONObject definescope = object.getJSONObject("defineScope");
            JSONObject scope = definescope.getJSONObject("scope");
            return scope.get("title").toString();
        }
        catch (JSONException e){
            return "ERROR: could not read name from json";
        }
    }

    public static String getSummary(JSONObject object){
        JSONObject reportFindings = object.getJSONObject("reportFindings");
        return reportFindings.getString("summary");

    }

    public static ArrayList<WcagKravDto> getKrav(JSONObject object){
        ArrayList<WcagKravDto> resultList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try{
            JSONArray auditSampleList = object.getJSONArray("auditSample");
            auditSampleList.forEach( a -> {
                        WcagKravDto wcagKrav = new WcagKravDto();
                        JSONObject auditSample = new JSONObject(a.toString());
                        JSONObject test = auditSample.getJSONObject("test");
                        wcagKrav.setId(
                                CriteriaMap.mapToReadAble(test.getString("id"))
                        );
                        wcagKrav.setDate(OffsetDateTime.parse(test.getString("date")));

                        JSONObject outcome = auditSample.getJSONObject("result").getJSONObject("outcome");
                        wcagKrav.setResult(outcome.getString("title"));
                        JSONObject subject = auditSample.getJSONObject("subject");
                        wcagKrav.setSubject(subject.getString("title"));


                        resultList.add(wcagKrav);

                    });
            return resultList;
        }
        catch (Exception e){
            return resultList;
        }
    }


}
