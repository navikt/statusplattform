package no.nav.statusplattform.api.wcag;

import no.nav.statusplattform.generated.api.KravMapEntryDto;
import no.nav.statusplattform.generated.api.WcagKravDto;
import no.nav.statusplattform.generated.api.WcagResultDto;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class WcagJsonParser {
    private static final Logger logger = LoggerFactory.getLogger(WcagJsonParser.class);


    public static List<String> getAllKravs(){
        List<WcagResultDto> resultDtos = readAllReports();
        ArrayList<String> alleKrav = new ArrayList<>();
        resultDtos.forEach(result -> {
            result.getCriterias().forEach(krav -> {
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
            result.getCriterias().forEach(krav -> {
                if(!alleKrav.contains(krav.getId())){
                    alleKrav.add(krav.getId());
                }
            });
        });
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        alleKrav.forEach(kravId -> {
            result.put(kravId, new ArrayList<>());
            resultDtos.forEach(resultDto -> {
                List<WcagKravDto> matching = resultDto.getCriterias().stream().filter(krav -> krav.getId().equals(kravId)).collect(Collectors.toList());
                if(matching.size()== 1) {
                   result.get(kravId).add(matching.getFirst().getSubject());
                }
                if(matching.size() > 1){
                    matching.forEach(resultOfKrav -> {
                                    result.get(kravId).add(resultDto.getServiceName() +", " + resultOfKrav.getSubject());

                            }

                    );
                }
            });

        });
        return result;
    }



    public static List<KravMapEntryDto> getAllKravsMapDto(){
        List<WcagResultDto> allReports = readAllReports();
        List<String> allCreteriasSorted = CriteriaMap.orderedCriterias;
        ArrayList<KravMapEntryDto> result = new ArrayList<>();
        allCreteriasSorted.forEach(kravId -> {
            ArrayList<WcagKravDto> listOfServicesWithCriteria = new ArrayList<>();
            allReports.forEach(report -> {
                List<WcagKravDto> matching = report.getCriterias().stream().filter(krav -> krav.getId().equals(kravId)).collect(Collectors.toList());
                if(matching.size() > 0){
                    matching.forEach(resultOfKrav -> {
                                if (!resultOfKrav.getSubject().equals(report.getServiceName())) {
                                    resultOfKrav.setSubject(report.getServiceName() + ", " + resultOfKrav.getSubject());
                                }
                                listOfServicesWithCriteria.add(resultOfKrav);
                            });
                }
            });
            KravMapEntryDto dto = new KravMapEntryDto();
            dto.setCriteriaName(kravId);
            dto.setServices(listOfServicesWithCriteria);
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
                    dto.setCriterias(dto.getCriterias().stream().sorted(Comparator.comparing(WcagKravDto::getId)).collect(Collectors.toList()))

        );
        return wcagResultDtos.stream().sorted(Comparator.comparing(r -> r.getServiceName().toLowerCase(Locale.ROOT))).collect(Collectors.toList());
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
        if (!path.contains("api")
        ) {
            path += "/api";
        }
        path+= "/src/main/resources/rapporter";
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
        wcagResultDto.setServiceName(name);
        List<WcagKravDto> kravArrayList = WcagJsonParser.getKrav(object);
        wcagResultDto.setCriterias(kravArrayList);
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
        try{
            JSONArray auditSampleList = object.getJSONArray("auditSample");
            auditSampleList.forEach( a -> {
                        WcagKravDto wcagKrav = new WcagKravDto();
                        JSONObject auditSample = new JSONObject(a.toString());
                        JSONObject test = auditSample.getJSONObject("test");
                        wcagKrav.setId(
                                CriteriaMap.mapToReadAble(test.getString("id"))
                        );
                        JSONObject result = auditSample.getJSONObject("result");
                        wcagKrav.setDate(OffsetDateTime.parse(result.getString("date")));
                        JSONObject outcome = result.getJSONObject("outcome");
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
