package pl.pasterz.demo.parkinglot.service.reservation;

import java.util.Date;
import javax.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pasterz.demo.parkinglot.converter.ParkingSpotToReservationConfirmConverter;
import pl.pasterz.demo.parkinglot.model.dto.CurrentFeeValue;
import pl.pasterz.demo.parkinglot.model.dto.ReservationConfirm;
import pl.pasterz.demo.parkinglot.model.entity.Spot;
import pl.pasterz.demo.parkinglot.model.error.InvalidDriverTypeException;
import pl.pasterz.demo.parkinglot.model.error.NoSpotAvailableException;
import pl.pasterz.demo.parkinglot.repository.DriverTypeRepository;
import pl.pasterz.demo.parkinglot.repository.ParkingSpotRepository;
import pl.pasterz.demo.parkinglot.repository.SpotsRepository;
import pl.pasterz.demo.parkinglot.model.dto.ClosingFeeValue;
import pl.pasterz.demo.parkinglot.model.entity.ParkingSpot;

@AllArgsConstructor
@Service
public class SpotReservationService implements ReservationService {

  @NonNull
  private final DriverTypeRepository driverTypeRepository;

  @NonNull
  private final SpotsRepository spotsRepository;

  @NonNull
  private final ParkingSpotRepository parkingSpotRepository;

  @NonNull
  private final ParkingSpotToReservationConfirmConverter reservationConfirmConverter;

  @Transactional
  @Override
  public ReservationConfirm postSpotReservation(String driver, String carNumber)
      throws RuntimeException {
    if (hasFreeSpot()) {
      throw new NoSpotAvailableException("No spot available at the moment");
    }

    ParkingSpot spot = new ParkingSpot(findFirstAvailableSpotId(), true);
    spot.setCarNumber(carNumber);
    spot.setStartDate(new Date());
    spot.setDriverType(driverTypeRepository
        .findById(driver)
        .orElseThrow(InvalidDriverTypeException::new));

    increaseByOne("occupied");
    decreaseByOne("free");

    return reservationConfirmConverter.apply(parkingSpotRepository.save(spot));
  }

  @Transactional
  @Override
  public ClosingFeeValue closeSpotReservation(int id, CurrentFeeValue response) {
    ClosingFeeValue closingFeeValue = new ClosingFeeValue();

    closingFeeValue.setFee(response.getFee());
    closingFeeValue.setId(id);

    deleteSpotReservation(id);

    return closingFeeValue;
  }

  private void deleteSpotReservation(int id) {
    ParkingSpot spot = new ParkingSpot(id, false);
    spot.setStartDate(null);
    spot.setDriverType(null);
    spot.setCarNumber(null);

    parkingSpotRepository.save(spot);

    decreaseByOne("occupied");
    increaseByOne("free");
  }

  private int findFirstAvailableSpotId() throws RuntimeException {
    return parkingSpotRepository
        .findAllByOccupiedOrderByIdAsc(false)
        .findFirst()
        .map(ParkingSpot::getId)
        .orElseThrow(RuntimeException::new);
  }

  private boolean hasFreeSpot() throws BadRequestException {
    return spotsRepository
        .findById("free")
        .map(Spot::getCountNumber)
        .orElseThrow(BadRequestException::new) == 0;
  }

  private void increaseByOne(String spotType) {
    Spot spot = spotsRepository
        .findById(spotType)
        .orElseThrow(RuntimeException::new);
    spot.setCountNumber(spot.getCountNumber() + 1);
    spotsRepository.save(spot);
  }

  private void decreaseByOne(String spotType) {
    Spot spot = spotsRepository
        .findById(spotType)
        .orElseThrow(RuntimeException::new);
    spot.setCountNumber(spot.getCountNumber() - 1);
    spotsRepository.save(spot);
  }
}
