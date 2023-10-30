package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Diary;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    //read one day
    List<Diary> findAllByDate(LocalDate date);
    //read many days
    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate);
    //update
    Diary getFirstByDate(LocalDate date);
    //delete
    @Transactional
    void deleteAllByDate(LocalDate date);
}
