package com.jd.workflow.soap.wsdl;

 import com.jd.workflow.soap.wsdl.param.Param;
 import com.jd.workflow.soap.wsdl.param.ParamType;
 import lombok.Data;



import java.util.List;
import java.util.stream.Collectors;

@Data
public class ServiceMethodInfo implements Comparable<ServiceMethodInfo>{
    String name;
    boolean rpc;
    /**
     * 输入参数
     */
    List<Param> inputParams;
    /**
     * 返回值参数
     */
    Param outParam;

    public ServiceMethodInfo(String name, boolean rpc) {
        this.name = name;
        this.rpc = rpc;
    }

    public boolean hasInput(){
        return inputParams!=null && !inputParams.isEmpty();
    }
    public boolean isVoidMethod(){
        return outParam == null;
    }
    public List<ParamType> getParameterTypes(){
        return inputParams.stream().map(vs->vs.getParamType()).collect(Collectors.toList());
    }

    /**
     *  确保每次生成的wsdl结果都一致
     * @param o
     * @return
     */
    @Override
    public int compareTo(ServiceMethodInfo o) {
        int val = getName().compareTo(o.getName());
        if (val == 0) {
            val = this.getInputParams().size() - o.getInputParams().size();
            if (val == 0) {
                List<ParamType> types1 = o.getParameterTypes();
                List<ParamType> types2 = o.getParameterTypes();
                for (int i = 0; i < types1.size(); i++) {
                    val = types1.get(i).ordinal() - types2.get(i).ordinal();

                    if (val != 0) {
                        break;
                    }
                }
            }
        }
        return val;
    }

    public Class<?> getReturnType() {
        if(outParam == null) return Void.class;
        return outParam.getParamType().getType();
    }
}
