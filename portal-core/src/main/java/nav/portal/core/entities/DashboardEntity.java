package nav.portal.core.entities;

import java.util.ArrayList;
import java.util.List;

public class DashboardEntity {

    private String name;
    private List<String> areasIds = new ArrayList<>();

    public DashboardEntity() {
    }

    public DashboardEntity(String name, List<String> areasIds) {
        this.name = name;
        this.areasIds = areasIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public List<String> getAreasIds() {
        return areasIds;
    }

    public void setAreasIds(List<String> areasIds) {
        this.areasIds = areasIds;
    }
}


