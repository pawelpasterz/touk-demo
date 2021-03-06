package pl.pasterz.demo.parkinglot.converter;

import java.util.function.Function;
import org.springframework.stereotype.Component;
import pl.pasterz.demo.parkinglot.model.dto.ReservationConfirm;
import pl.pasterz.demo.parkinglot.model.entity.ParkingSpot;

@Component
public class ParkingSpotToReservationConfirmConverter implements
    Function<ParkingSpot, ReservationConfirm> {

  @Override
  public ReservationConfirm apply(ParkingSpot spot) {
    return new ReservationConfirm(spot.getId(),
        spot
            .getDriverType()
            .getRoleName(),
        spot.getStartDate()
    );
  }
}
