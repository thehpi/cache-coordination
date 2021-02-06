package nl.thehpi.entities;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheCoordinationType;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Cacheable(true)
@Cache(coordinationType =  CacheCoordinationType.SEND_OBJECT_CHANGES)
//@Cache(coordinationType =  CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS)
//@Cache(coordinationType =  CacheCoordinationType.SEND_NEW_OBJECTS_WITH_CHANGES)
//@Cache(coordinationType =  CacheCoordinationType.NONE)
public class World {
    @Id
    private String id;

    private String field1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }
}

