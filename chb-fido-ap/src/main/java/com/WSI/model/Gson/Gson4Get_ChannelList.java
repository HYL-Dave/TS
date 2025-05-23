package com.WSI.model.Gson;

import java.util.List;

import com.Channel.model.ChannelVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4Get_ChannelList extends Gson4Common {
	private List<ChannelVO> channel;

}
