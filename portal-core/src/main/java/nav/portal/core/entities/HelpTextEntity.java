package nav.portal.core.entities;

import nav.portal.core.enums.ServiceType;

import java.util.Objects;

public class HelpTextEntity {
    private int number; // nr represents number
    private ServiceType type;
    private String content;

    public int getNumber() {
        return number;
    }

    public HelpTextEntity setNumber(int number) {
        this.number = number;
        return this;
    }

    public ServiceType getType() {
        return type;
    }

    public HelpTextEntity setType(ServiceType type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }

    public HelpTextEntity setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HelpTextEntity that)) return false;
        return getNumber() == that.getNumber() && getType() == that.getType() && Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumber(), getType(), getContent());
    }
}
