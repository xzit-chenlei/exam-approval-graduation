package edu.xzit.core.util;

import edu.xzit.core.core.common.core.domain.entity.SysDictData;
import edu.xzit.core.dao.service.ISysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DictUtil {

    @Autowired
    private ISysDictDataService iSysDictDataService;

    public Map<String, String> getDictValueLabelMap(String dictType) {
        SysDictData carAttributeDictParam = new SysDictData();
        carAttributeDictParam.setDictType(dictType);
        return iSysDictDataService.selectDictDataList(carAttributeDictParam).stream()
                .collect(Collectors.toMap(SysDictData::getDictValue, SysDictData::getDictLabel));
    }

    public Map<String, Object> getDictLabelValueMap(String dictType) {
        SysDictData carAttributeDictParam = new SysDictData();
        carAttributeDictParam.setDictType(dictType);
        return iSysDictDataService.selectDictDataList(carAttributeDictParam).stream()
                .collect(Collectors.toMap(SysDictData::getDictLabel, SysDictData::getDictValue));
    }
}
