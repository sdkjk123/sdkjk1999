package com.qhit.fsaas.util;

import com.qhit.fsaas.bo.*;
import com.qhit.fsaas.service.IAllocationService;
import com.qhit.fsaas.service.ISeatsService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

@Component
public class MainUtil {
    @Resource
    ISeatsService seatsService;
    @Resource
    IAllocationService allocationService;
    @Resource
    private RedisUtil redisUtil;

    public void init() {
        List<Seat> seatList = new ArrayList<>();
        HashMap<Long, LinkedList<Seat>> seatHash = new HashMap<>();

        redisUtil.setTid(redisUtil.getTid());
        initSeat(seatsService.selectAll(redisUtil.getTid()), seatList,seatHash);
        redisUtil.setSeatList(seatList);
        redisUtil.setSeatHash(seatHash);
        redisUtil.setAllocationList(allocationService.selectAll(redisUtil.getTid()));
    }

    private void initSeat(List<Seats> seatsList, List<Seat> seatList, HashMap<Long, LinkedList<Seat>> seatHash) {
        long[] seatCount = new long[seatsList.size() - 11];

        int a = seatsList.size() - 11 - 1;
        int num = 1;
        for (int i = seatsList.size()-1; i >= 0; i--) {
            Seats seats = seatsList.get(i);
            if (!seats.getSColumn().equals("")) {
                if(seats.getAssigned()==0){//0 已分配
                    num = 0;
                }
                seatList.add(0,new Seat(seats.getSid(),seats.getSColumn(), seats.getSRow(), seats.getV_window(), seats.getV_aisle(), seats.getV_gate(), seats.getV_basket(), a,seats.getTid(), seats.getFlag()));
                seatCount[a--] = num++;
            }
        }
        showSeatCount(seatCount);
        redisUtil.setSeatCount(seatCount);
        addToSeatHashMap(seatList, seatHash);
    }

    public void addToSeatHashMap(List<Seat> seatList, HashMap<Long, LinkedList<Seat>> seatHash) {
        for (Seat seat : seatList) {
            if ((seat.getFlg() & Global.SEAT_STATUS_NOASSIGNED) != Global.SEAT_STATUS_NOASSIGNED) {
                continue;
            }
            LinkedList<Seat> seats = seatHash.get(seat.getFlg());
            if (seats == null || seats.size() == 0) {
                seats = new LinkedList<>();
                seats.add(seat);
                seatHash.put(seat.getFlg(), seats);
            } else {
                seats.add(seat);
            }
        }
    }

    public void showSeatCount() {
        long[] seatCount = redisUtil.getSeatCount();
        for (int i = 0; i < seatCount.length; i++) {
            if (i % 20 == 0) System.out.println();
            System.out.print(seatCount[i] + "\t");
        }
        System.out.println("\n---------------------------------------------------------------------------------");
    }

    private void showSeatCount(long[] seatCount) {
        for (int i = 0; i < seatCount.length; i++) {
            if (i % 20 == 0) System.out.println();
            System.out.print(seatCount[i] + "\t");
        }
        System.out.println("\n---------------------------------------------------------------------------------");
    }

    public boolean batchInsert(List<Passengers> list){
        List<Allocation> allocationList = new ArrayList<>();
        for (Passengers passengers : list) {
            Integer passenger_num = passengers.getPassenger_num();
            List<Passenger> passenger_info = passengers.getPassenger_info();
            for (int j = 0; j < passenger_num; j++) {
                Passenger passenger = passenger_info.get(j);
                Seat seat = passenger.getSeat();
                if(seat!=null&&seat.getSid()>0){
                    allocationList.add(new Allocation(seat.getSid(), passenger.getPName(), passenger.getGroupId()));
                }
            }
        }
        int i = allocationService.batchInsert(allocationList);
        if (i>0){
            List<Allocation> allocationListOfRedis = redisUtil.getAllocationList();
            allocationListOfRedis.addAll(allocationList);
            redisUtil.setAllocationList(allocationListOfRedis);
            return true;
        }else{
            return false;
        }
    }
}