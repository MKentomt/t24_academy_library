package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import java.util.Date;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
        List<RentalManage> findAll();

        Optional<RentalManage> findById(Long id);

        @Query("select r from RentalManage r where r.stock.id = ?1 and r.status in (0,1)")
        List<RentalManage> findByStockId(String StockId);

        // タイトルに紐づく貸出予定日＝＜day ＆＆ day＝＜返却予定日の数をカウント
        @Query(value = "SELECT COUNT(*) AS count " +
                        "FROM rental_manage rm " +
                        "JOIN stocks s ON rm.stock_id = s.id " +
                        "JOIN book_mst bm ON s.book_id = bm.id " +
                        "WHERE rm.expected_rental_on <= :day AND :day <= rm.expected_return_on AND bm.title = :title", nativeQuery = true)
        Long findByUnAvailableCount(@Param("day") Date day, @Param("title") String title);

        @Query(value = "SELECT st.id " +
                        "FROM stocks st " +
                        "JOIN book_mst bm ON st.book_id = bm.id " +
                        "LEFT JOIN rental_manage rm ON st.id = rm.stock_id " +
                        "WHERE (rm.expected_rental_on > :day OR rm.expected_return_on < :day OR rm.stock_id IS NULL) "
                        +
                        "AND bm.title = :title " +
                        "AND st.status = '0' ", nativeQuery = true)
        List<String> findByAvailableStockId(@Param("day") Date day, @Param("title") String title);

}
