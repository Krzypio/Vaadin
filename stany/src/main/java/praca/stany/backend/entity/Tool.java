package praca.stany.backend.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Tool extends AbstractEntity {

    @NotNull
    private String name;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Tool> children = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Tool parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Tool> getChildren() {
        return children;
    }

    public void setChildren(Set<Tool> children) {
        this.children = children;
    }

    public Tool getParent() {
        return parent;
    }

    public void setParent(Tool parent) {
        this.parent = parent;
    }

    public Tool() {
    }

    public Tool(@NotNull String name) {
        this.name = name;
    }

    public Tool(@NotNull String name, Tool parent) {
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

    public Set<Tool> getAllDescendants() {
        Set<Tool> descendants = getChildren();
        for (Tool descendant : descendants) {
            descendants.addAll(descendant.getAllDescendants());
        }//for
        return descendants;
    }

    public Set<Tool> getAllAncestors(){
        return getAllAncestors(this);
    }

    private Set<Tool> getAllAncestors(Tool node){
        Set<Tool> ancestors = new HashSet<>();
        if(node.getParent() != null){
            ancestors.add(node.getParent());
            for (Tool ancestor: ancestors) {
                ancestors.addAll(ancestor.getAllAncestors(ancestor));
            }
        }
        return ancestors;
    }

    public int getAncestorsNumber(){
        return getAncestorsNumber(this);
    }

    private int getAncestorsNumber(Tool container){
        int no = 0;
        if (container.getParent() != null) {
            no += 1 + getAncestorsNumber(container.getParent());
        }
        return no;
    }
}
