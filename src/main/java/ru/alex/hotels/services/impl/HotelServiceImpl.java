package ru.alex.hotels.services.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.alex.hotels.dto.HotelDto;
import ru.alex.hotels.entity.City;
import ru.alex.hotels.entity.Director;
import ru.alex.hotels.entity.Hotel;
import ru.alex.hotels.exceptions.EntityAlreadyExistException;
import ru.alex.hotels.exceptions.EntityNotFoundException;
import ru.alex.hotels.mappers.HotelMapper;
import ru.alex.hotels.repositories.HotelRepository;
import ru.alex.hotels.services.CityService;
import ru.alex.hotels.services.DirectorService;
import ru.alex.hotels.services.HotelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final DirectorService directorService;
    private final CityService cityService;
    private final HotelMapper hotelMapper;

    @Override
    @Transactional
    public HotelDto createHotel(@Valid HotelDto hotelDto, String city, Long directorId) {
        City cityEntity = cityService.getCityEntityByName(city);

        Director directorEntity = directorService.getDirectorEntityById(directorId);

        Optional<Hotel> hotelEntity = hotelRepository.findByName(hotelDto.getName());
        Hotel hotelEntityForSave;

        if (hotelEntity.isPresent()) {
            if (cityEntity.getHotels().contains(hotelEntity.get())) {
                throw new EntityAlreadyExistException("Отель с именем = " + hotelDto.getName() +
                        " уже сущствует в городе " + cityEntity.getName());
            }
            else {
                addHotelInCity(cityEntity, hotelEntity.get());
                hotelEntityForSave = hotelEntity.get();
                hotelEntityForSave.setDirector(directorEntity);
            }
        }
        else {
            hotelEntityForSave = createHotelEntityAndSetInCity(hotelDto, cityEntity);
        }

        return hotelMapper.toDto(hotelRepository.save(hotelEntityForSave));
    }

    @Override
    @Transactional
    public List<HotelDto> getAllHotels() {
        return hotelMapper.toDtoList(hotelRepository.findAll());
    }

    @Override
    public HotelDto getHotelById(Long id) {
        return hotelMapper.toDto(getHotelEntityById(id));
    }

    @Override
    public Hotel getHotelEntityById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("отель с id = " + id + " не найден"));
    }

    @Override
    public HotelDto updateHotel(@Valid HotelDto hotelDto, Long id) {
        Hotel hotelEntity = getHotelEntityById(id);
        hotelEntity.setName(hotelDto.getName());

        return hotelMapper.toDto(hotelRepository.save(hotelEntity));
    }

    @Override
    @Transactional
    public List<HotelDto> getAllHotelsInCity(String cityName) {
        City desiredCity = cityService.getCityEntityByName(cityName);

        List<Hotel> hotelEntities = hotelRepository.findAllHotelInCity(desiredCity.getId());

        return hotelMapper.toDtoList(hotelEntities);
    }

    @Override
    @Transactional
    public boolean isHotelExist(Long hotelId) {
        return hotelRepository.existsById(hotelId);
    }

    private Hotel createHotelEntityAndSetInCity(HotelDto hotelDto, City cityEntity) {
        Hotel hotelToEntity = hotelMapper.toEntity(hotelDto);
        hotelToEntity.setCities(new ArrayList<>());

        cityEntity.getHotels().add(hotelToEntity);

        return hotelToEntity;
    }

    private void addHotelInCity(City cityEntity, Hotel hotelEntity) {
        cityEntity.getHotels().add(hotelEntity);
    }

}
