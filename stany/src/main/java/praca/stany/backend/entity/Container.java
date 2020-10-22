package praca.stany.backend.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
public class Container extends AbstractEntity {

    @NotNull
    private String name;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Container> children = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Container parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Container> getChildren() {
        return children;
    }

    public void setChildren(Set<Container> children) {
        this.children = children;
    }

    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public Container() {
    }

    public Container(@NotNull String name) {
        this.name = name;
    }

    public Container(@NotNull String name, Container parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getHierarchicalName(){
        String hierarchicalName;
        if (getParent() != null)
            return  getParent().getHierarchicalName() + ">" + getName();
        else
            return getName();
    }

    public Set<Container> getAllDescendants() {
        Set<Container> descendants = getChildren();
        for (Container descendant : descendants) {
            descendants.addAll(descendant.getAllDescendants());
        }//for
        return descendants;
    }

    public Set<Container> getAllAncestors(){
        return getAllAncestors(this);
    }

    private Set<Container> getAllAncestors(Container node){
        Set<Container> ancestors = new HashSet<>();
        if(node.getParent() != null){
            ancestors.add(node.getParent());
            for (Container ancestor: ancestors) {
                ancestors.addAll(ancestor.getAllAncestors(ancestor));
            }
        }
        return ancestors;
    }

    public int getAncestorsNumber(){
        return getAncestorsNumber(this);
    }

    private int getAncestorsNumber(Container container){
        int no = 0;
        if (container.getParent() != null) {
            no += 1 + getAncestorsNumber(container.getParent());
        }
        return no;
    }
}
