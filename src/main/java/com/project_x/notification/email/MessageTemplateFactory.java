package com.project_x.notification.email;

import com.project_x.notification.model.Param;
import org.springframework.util.ObjectUtils;
import org.thymeleaf.context.Context;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageTemplateFactory {
    private static MessageTemplateFactory messageTemplateFactory;

    /**
     *
     * @param params
     * @return
     */
    public Map<String,Object> resolveParamsToMap(List<Param> params){
        if(!ObjectUtils.isEmpty(params)) {
            return params.stream().filter(param -> (!param.getName().isEmpty()
                            && !param.getValue().isEmpty()))
                    .collect(Collectors.toMap(Param::getName, Param::getValue));
        }
        return new HashMap<>();
    }

    public Map<String,String> resolveParamsToStringMap(List<Param> params){
        if(!ObjectUtils.isEmpty(params)) {
            return params.stream().filter(param -> (!param.getName().isEmpty()
                            && !param.getValue().isEmpty()))
                    .collect(Collectors.toMap(Param::getName, Param::getValue));
        }
        return new HashMap<>();
    }

    /**
     *
     * @param paramMap
     * @param param
     */
    public void addParamToMap(Map<String,Object> paramMap, Param param){
        paramMap.put(param.getName(),param.getValue());
    }


    public Context generateContextOutOfMap(Map<String,Object> paramMap){
        Context context = new Context();
        if(paramMap==null){
            paramMap = new HashMap<>();
        }
        paramMap.put("currentYear", Integer.toString(Year.now().getValue()));
        context.setVariables(paramMap);
        return context;
    }

}
