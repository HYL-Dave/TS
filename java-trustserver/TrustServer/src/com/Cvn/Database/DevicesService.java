package com.Cvn.Database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DevicesService {

    @Autowired
    DevicesRepo devicesRepo;

    private Logger logger = LogManager.getLogger(DevicesService.class);

    public List<Devices> list() {
        return devicesRepo.findAll();
    }

    public String getNewEsnSeq(String sessionId) {
        try {
            String newEsnSeq = devicesRepo.getNextESN();
            logger.info("new esn:{}", newEsnSeq);
            return newEsnSeq;
        } catch (Exception e) {
            logger.error("[{}][iDGate-getNewEsnSeq] - #DB Error occurred: ", sessionId, e);
            return e.getMessage();
        }
    }

    public String createDevice(String userID, String esn, String devData, String mercury, String status,
                               String devID, String xPH, String sessionId) {
        try {
            Devices dev = new Devices();
            dev.setUserID(userID);
            dev.setEsn(esn);
            dev.setDevData(devData);
            dev.setMercury(mercury);
            dev.setStatus(status);
            dev.setDevID(devID);
            dev.setXph(xPH);
            dev.setErrCount(0);
            dev.setChkCount(0);
            dev.setErrMax(10);
            devicesRepo.saveAndFlush(dev);
            return "0";
        } catch (Exception e) {
            logger.error("[{}][iDGate-createDevice] - #DB Error occurred: " ,sessionId,e);
            return e.getMessage();
        }
    }

    public String updateDevice(String userID, String esn, String devData, String mercury, String status,
                               String devID, String xPH, String sessionId) {

        try {
            Devices dev = devicesRepo.findByUserID(userID);
            if (dev==null) {
                dev = new Devices();
            }
            dev.setUserID(userID);
            dev.setEsn(esn);
            dev.setDevData(devData);
            dev.setMercury(mercury);
            dev.setStatus(status);
            dev.setDevID(devID);
            dev.setXph(xPH);
            devicesRepo.saveAndFlush(dev);
            return "0";
        } catch (Exception e) {
            logger.error("[{}][iDGate-updateDevice] - #DB Error occurred: ", sessionId, e);
            return e.getMessage();
        }
    }

    public String[] getDevice_UserID(String userID, String sessionId) {
        try {
            Devices dev = devicesRepo.findByUserID(userID);
            if (dev==null) {
                String[] rsp = {""};
                return rsp;
            }else {
                String[] rsp = {dev.getEsn(), dev.getDevData(), dev.getMercury(),
                        String.valueOf(dev.getErrCount()), String.valueOf(dev.getErrMax()), dev.getStatus(),
                        dev.getDevID()};
                return rsp;
            }
        } catch (Exception e) {
            logger.error("[{}][iDGate-getDevice_UserID] - #DB Error occurred: ", sessionId,e);
            return new String[] { e.getMessage() };
        }
    }


    public String[] getDevice_Esn(String esn, String sessionId) {
        try {
            Devices dev = devicesRepo.findByEsn(esn);
            String[] rsp = {dev.getUserID(), dev.getDevData(), dev.getMercury(), String.valueOf(dev.getErrCount()), String.valueOf(dev.getErrMax()), dev.getStatus(), dev.getDevID()};
            return rsp;
        } catch (Exception e) {
            logger.error("[{}][iDGate-getDevice_Esn] - #DB Error occurred: " ,sessionId,e);
            return new String[] { e.getMessage() };
        }

    }

    public String updateDevice_Status(String esn, String status, String sessionId) {
        try {
            Devices dev = devicesRepo.findByEsn(esn);
            dev.setStatus(status);
            devicesRepo.saveAndFlush(dev);
            return "0";
        } catch (Exception e) {
            logger.error("[{}][iDGate-updateDevice_Status] - #DB Error occurred: " ,sessionId,e);
            return e.getMessage();
        }
    }

    public String updateDevice_Mercury(String esn, String mercury, int newChkCount, String sessionId) {

        try {
            Devices dev = devicesRepo.findByEsn(esn);
            dev.setMercury(mercury);
            dev.setChkCount(newChkCount);
            devicesRepo.saveAndFlush(dev);
            return "0";
        }catch (Exception e) {
            logger.error("[{}][iDGate-updateDevice_Mercury] - #DB Error occurred: " + sessionId, e);
            return e.getMessage();
        }
    }

}
