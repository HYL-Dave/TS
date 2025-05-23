package com.Cvn.Database;

import com.toppanidgate.idenkey.common.model.ReturnCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ChannelTrustService {

    private Logger logger = LogManager.getLogger(ChannelTrustService.class);

    @Autowired
    ChannelTrustRepo channelTrustRepo;

    public List<ChannelTrust> list() {
        return channelTrustRepo.findAll();
    }

    public HashMap<String, Object> getChannelById(String id, String sessionId) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            logger.info("ChannelTrustService channel: {}",id);
            ChannelTrust ch = channelTrustRepo.findByChannelID(id);
            if (ch != null) {
                logger.info("Channel info : {}",ch);
                result.put("OTP_Length", ch.getOtpLength());
                result.put("OTP_Range", ch.getOtpRange());
                result.put("OTP_Interval", ch.getOtpIntrval());
                result.put("ReturnCode", "00000");
            } else {
                result.put("No_Data", true);
            }
            return result;
        } catch (Exception e) {
            logger.error("[{}][iDGate-getChannelById] - #DB Error occurred: " ,sessionId,e);
            result = new HashMap<String, Object>();
            result.put("ReturnCode", "0008");
            result.put("ReturnMsg", e.getMessage());
            return result;
        }
    }

    public HashMap<String, Object> getChannelByName(String channelName, String sessionId) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            ChannelTrust ch = channelTrustRepo.findByChannelName(channelName);
            if (ch != null) {
                result.put("OTP_Length", ch.getOtpLength());
                result.put("OTP_Range", ch.getOtpRange());
                result.put("OTP_Interval", ch.getOtpIntrval());
            } else {
                result.put("No_Data", true);
            }
            return result;
        } catch (Exception e) {
            logger.error("[{}][iDGate-getChannelByName] - #DB Error occurred: " ,sessionId,e);
            result = new HashMap<String, Object>();
            result.put("ReturnCode", "0008");
            result.put("ReturnMsg", e.getMessage());
            return result;
        }
    }

    public HashMap<String, Object> getChannelList(String sessionId) {
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> tmp = null, result = new HashMap<String, Object>();
        try {
            List<ChannelTrust> chs = channelTrustRepo.findAll();
            for (ChannelTrust ch : chs) {
                tmp = new HashMap<String, Object>();
                tmp.put("Channel_ID", ch.getChannelID());
                tmp.put("Channel_Name", ch.getChannelName());
                tmp.put("OTP_Length", ch.getOtpLength());
                tmp.put("OTP_Range", ch.getOtpRange());
                tmp.put("OTP_Interval", ch.getOtpIntrval());
                tmp.put("Create_Date", ch.getCreateDate());
                tmp.put("Last_Modified", ch.getModified());
                list.add(tmp);
            }
            result.put("Data", list);
            result.put("ReturnCode", ReturnCode.Success);
            result.put("ReturnMsg", "Success");
            return result;
        } catch (Exception e) {
            logger.error("[{}][iDGate-getChannelList] - #DB Error occurred: " ,sessionId,e);
            result.put("ReturnCode", "0008");
            result.put("ReturnMsg", e.getMessage());
            return result;
        }
    }

    public HashMap<String, Object> addChannel(String id, String name, int otpLength, int otpRange,int otpInterval, String sessionId) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try{
            ChannelTrust ch = new ChannelTrust();
            ch.setChannelID(id);
            ch.setChannelName(name);
            ch.setOtpRange(otpRange);
            ch.setOtpLength(otpLength);
            ch.setOtpIntrval(otpInterval);
            channelTrustRepo.saveAndFlush(ch);
            result.put("ReturnCode", ReturnCode.Success);
            result.put("ReturnMsg", "Success");
            return result;
        } catch (Exception e) {
            logger.error("[{}][iDGate-addChannel] - #DB Error occurred: " ,sessionId,e);
            result.put("ReturnCode", "0008");
            result.put("ReturnMsg", e.getMessage());
            return result;
        }
    }


}
