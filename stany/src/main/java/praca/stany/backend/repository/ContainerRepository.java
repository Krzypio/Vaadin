package praca.stany.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import praca.stany.backend.entity.Container;

import java.util.List;

public interface ContainerRepository extends JpaRepository<Container, Long> {
    @Query("select c from Container c " +
            "where lower(c.name) like lower(concat('%', :searchTerm, '%')) ")
    List<Container> search(@Param("searchTerm") String searchTerm);
}
