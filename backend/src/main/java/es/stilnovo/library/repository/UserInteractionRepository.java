package es.stilnovo.library.repository;

import es.stilnovo.library.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
}