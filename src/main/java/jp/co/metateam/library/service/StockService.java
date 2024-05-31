package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.CalenderDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import java.util.Date;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<Object[]> findAllAvailableStockCounts() {
        return this.stockRepository.findAllAvailableStockCounts();
    }

    @Transactional
    public Long findByUnAvailableCount(Date day, String title) {
        return this.rentalManageRepository.findByUnAvailableCount(day, title);
    }

    @Transactional
    public List<String> findByAvailableStockId(Date day, String title) {
        return this.rentalManageRepository.findByAvailableStockId(day, title);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<List<CalenderDto>> generateValues(Integer year, Integer month, Integer daysInMonth) {
        List<Object[]> titleAndStatus = findAllAvailableStockCounts();

        List<List<CalenderDto>> stockDatasLists = new ArrayList<>();

        for (Object[] data : titleAndStatus) {
            List<CalenderDto> stockDataList = new ArrayList<>();
            String title = (String) data[0];
            Long stockNum = (Long) data[1];

            for (int i = 1; i <= daysInMonth; i++) {
                CalenderDto stockCalenderDto = new CalenderDto();
                stockCalenderDto.setTitle(title);
                stockCalenderDto.setStockNum(stockNum);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, i);

                // 時間を切り捨てる
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                Date day = calendar.getTime();
                stockCalenderDto.setExpectedRentalOn(day);

                Long unAvailableCount = findByUnAvailableCount(day, title);
                List<String> AvailStockIdList = findByAvailableStockId(day, title);
                if (!AvailStockIdList.isEmpty()) {
                    stockCalenderDto.setStockId(AvailStockIdList.get(0));
                }

                Long dayStockNum = stockNum - unAvailableCount;
                if (dayStockNum == 0) {
                    String dayStockNoNum = "×";
                    stockCalenderDto.setDayStockNum(dayStockNoNum);
                } else {
                    stockCalenderDto.setDayStockNum(dayStockNum);
                }
                stockDataList.add(stockCalenderDto);
            }
            stockDatasLists.add(stockDataList);
        }

        return stockDatasLists;
    }

}
