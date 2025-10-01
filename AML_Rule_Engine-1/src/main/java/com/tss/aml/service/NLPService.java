// com.tss.aml.service.NLPService.java

package com.tss.aml.service;

import java.util.Map;

public interface NLPService {
    Map<String, Object> analyzeText(String text);
}