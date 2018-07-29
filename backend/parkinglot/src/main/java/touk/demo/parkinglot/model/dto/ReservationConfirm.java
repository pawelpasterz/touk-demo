package touk.demo.parkinglot.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReservationConfirm {

  private int spotId;
  private String driverType;

  @JsonFormat(pattern = "yyyy-MM-dd@HH:mm:ss")
  @DateTimeFormat(pattern = "yyyy-MM-dd@HH:mm:ss")
  private Date beginTime;
}
