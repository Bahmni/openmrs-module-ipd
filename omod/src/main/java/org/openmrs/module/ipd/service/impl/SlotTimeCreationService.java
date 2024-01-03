package org.openmrs.module.ipd.service.impl;

import org.openmrs.DrugOrder;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.model.DrugOrderSchedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.contract.ScheduleMedicationRequest.MedicationFrequency.FIXED_SCHEDULE_FREQUENCY;
import static org.openmrs.module.ipd.contract.ScheduleMedicationRequest.MedicationFrequency.START_TIME_DURATION_FREQUENCY;

@Service
@Component
public class SlotTimeCreationService extends BaseOpenmrsService {

    public static final List<String> START_TIME_FREQUENCIES= Arrays.asList(new String[]{"Every Hour", "Every 2 hours", "Every 3 hours", "Every 4 hours", "Every 6 hours", "Every 8 hours", "Every 12 hours", "Once a day", "Nocte (At Night)", "Every 30 minutes", "STAT (Immediately)"});

    public List<LocalDateTime> createSlotsStartTimeFrom(ScheduleMedicationRequest request, DrugOrder order) {
        if (request.getSlotStartTimeAsLocaltime() != null && request.getMedicationFrequency() == START_TIME_DURATION_FREQUENCY) {
            return getSlotsStartTimeWithStartTimeDurationFrequency(request, order);
        } else if (!CollectionUtils.isEmpty(request.getDayWiseSlotsStartTimeAsLocalTime()) && request.getMedicationFrequency() == FIXED_SCHEDULE_FREQUENCY) {
            return getSlotsStartTimeWithFixedScheduleFrequency(request, order);
        }

        return Collections.emptyList();
    }

    private List<LocalDateTime> getSlotsStartTimeWithFixedScheduleFrequency(ScheduleMedicationRequest request, DrugOrder order) {
        int numberOfSlotsStartTimeToBeCreated = (int) (Math.ceil(order.getQuantity() / order.getDose()));

        List<LocalDateTime> slotsStartTime = new ArrayList<>();

        if (!CollectionUtils.isEmpty(request.getFirstDaySlotsStartTimeAsLocalTime())) {
            List<LocalDateTime> slotsToBeAddedForFirstDay = numberOfSlotsStartTimeToBeCreated < request.getFirstDaySlotsStartTimeAsLocalTime().size()
                ? request.getFirstDaySlotsStartTimeAsLocalTime().subList(0, numberOfSlotsStartTimeToBeCreated)
                : request.getFirstDaySlotsStartTimeAsLocalTime();

            slotsStartTime.addAll(slotsToBeAddedForFirstDay);
            numberOfSlotsStartTimeToBeCreated -= slotsToBeAddedForFirstDay.size();
        }

        if (!CollectionUtils.isEmpty(request.getDayWiseSlotsStartTimeAsLocalTime()) && numberOfSlotsStartTimeToBeCreated > 0) {

            List<LocalDateTime> initialSlotsToBeAddedForSecondDay = numberOfSlotsStartTimeToBeCreated < request.getDayWiseSlotsStartTimeAsLocalTime().size()
                    ? request.getDayWiseSlotsStartTimeAsLocalTime().subList(0, numberOfSlotsStartTimeToBeCreated)
                    : request.getDayWiseSlotsStartTimeAsLocalTime();
            slotsStartTime.addAll(initialSlotsToBeAddedForSecondDay);
            numberOfSlotsStartTimeToBeCreated -= initialSlotsToBeAddedForSecondDay.size();

            List<LocalDateTime> nextSlotsStartTime = request.getDayWiseSlotsStartTimeAsLocalTime();
            List<LocalDateTime> remainingDaySlotsStartTime = request.getRemainingDaySlotsStartTimeAsLocalTime();
            while (numberOfSlotsStartTimeToBeCreated > 0) {
                nextSlotsStartTime = nextSlotsStartTime.stream().map(slotStartTime -> slotStartTime.plusHours(24)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(remainingDaySlotsStartTime) && numberOfSlotsStartTimeToBeCreated <= remainingDaySlotsStartTime.size()){
                    List<LocalDateTime> slotsToBeAddedForRemainingDay = numberOfSlotsStartTimeToBeCreated < remainingDaySlotsStartTime.size()
                            ? remainingDaySlotsStartTime.subList(0, numberOfSlotsStartTimeToBeCreated)
                            : remainingDaySlotsStartTime;
                    numberOfSlotsStartTimeToBeCreated -= slotsToBeAddedForRemainingDay.size();
                    slotsStartTime.addAll(slotsToBeAddedForRemainingDay);
                }
                else if (numberOfSlotsStartTimeToBeCreated >= nextSlotsStartTime.size()) {
                    slotsStartTime.addAll(nextSlotsStartTime);
                    numberOfSlotsStartTimeToBeCreated -= nextSlotsStartTime.size();
                } else {
                    slotsStartTime.addAll(nextSlotsStartTime.subList(0, numberOfSlotsStartTimeToBeCreated));
                    numberOfSlotsStartTimeToBeCreated -= nextSlotsStartTime.subList(0, numberOfSlotsStartTimeToBeCreated).size();
                }
            }
        }

        return slotsStartTime;
    }

    private List<LocalDateTime> getSlotsStartTimeWithStartTimeDurationFrequency(ScheduleMedicationRequest request, DrugOrder order) {
        int numberOfSlotsStartTimeToBeCreated = (int) (Math.ceil(order.getQuantity() / order.getDose()));
        List<LocalDateTime> slotsStartTime = new ArrayList<>();
        Double slotDurationInHours =  24 / order.getFrequency().getFrequencyPerDay();
        LocalDateTime slotStartTime = request.getSlotStartTimeAsLocaltime();
        while (numberOfSlotsStartTimeToBeCreated-- > 0) {
            slotsStartTime.add(slotStartTime);
            if(slotDurationInHours.compareTo(1.0) >= 0)
            {
                slotStartTime = slotStartTime.plusHours(slotDurationInHours.longValue());
            }
            else {
                Double minutesToBeAdded = 60 * slotDurationInHours;
                slotStartTime = slotStartTime.plusMinutes(minutesToBeAdded.longValue());
            }
        }
        return slotsStartTime;
    }

    public HashMap<String , DrugOrderSchedule> getDrugOrderScheduledTime(Map<DrugOrder,List<Slot>> slotsByOrder){
        HashMap<String, DrugOrderSchedule> drugOrderScheduleHash= new HashMap<>();
        for (DrugOrder drugOrder : slotsByOrder.keySet()) {
            Double frequencyPerDay = drugOrder.getFrequency().getFrequencyPerDay();
            String frequency=drugOrder.getFrequency().getName();
            Map<LocalDate, List<Long>> groupedByDateAndEpoch = slotsByOrder.get(drugOrder).stream()
                    .collect(Collectors.groupingBy(
                            obj -> obj.getStartDateTime().toLocalDate(),
                            Collectors.mapping(
                                    obj -> obj.getStartDateTime().toEpochSecond(java.time.ZoneOffset.UTC),
                                    Collectors.toList()
                            )
                    ));

            List<List<Long>> sortedList = groupedByDateAndEpoch.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Sort by LocalDate in ascending order
                    .map(Map.Entry::getValue) // Get the list of Longs for each entry
                    .collect(Collectors.toList()); // Collect the list of lists into a single ArrayList

            DrugOrderSchedule drugOrderSchedule=new DrugOrderSchedule();
            if (START_TIME_FREQUENCIES.contains(frequency)){
                drugOrderSchedule.setSlotStartTime(sortedList.get(0).get(0));
            }
            else if (sortedList.get(0).size() == frequencyPerDay || (sortedList.size() == 1)) {
                drugOrderSchedule.setDayWiseSlotsStartTime(sortedList.get(0));
            } else {
                drugOrderSchedule.setFirstDaySlotsStartTime(sortedList.get(0));
                drugOrderSchedule.setRemainingDaySlotsStartTime(sortedList.get(sortedList.size() - 1));
                if (sortedList.size() > 2) {
                    drugOrderSchedule.setDayWiseSlotsStartTime(sortedList.get(1));
                }
            }
            drugOrderSchedule.setSlots(slotsByOrder.get(drugOrder));
            drugOrderScheduleHash.put(drugOrder.getUuid(),drugOrderSchedule);
        }
        return drugOrderScheduleHash;
    }
}
