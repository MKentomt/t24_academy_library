package jp.co.metateam.library.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalenderDto {

    private String title;

    private Long stockNum;

    private Date expectedRentalOn;

    private String stockId;

    private Object dayStockNum;

}
