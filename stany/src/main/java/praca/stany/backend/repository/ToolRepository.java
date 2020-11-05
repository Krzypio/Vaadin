package praca.stany.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import praca.stany.backend.entity.Container;
import praca.stany.backend.entity.Tool;

import java.util.List;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    @Query("select c from Container c " +
            "where lower(c.name) like lower(concat('%', :searchTerm, '%')) ")
    List<Tool> search(@Param("searchTerm") String searchTerm);
}
