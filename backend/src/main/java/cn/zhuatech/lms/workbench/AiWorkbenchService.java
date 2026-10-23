/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.lms.workbench;

import static cn.zhuatech.lms.workbench.WorkbenchInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);

    keys(r, "completed", "courses");
    Map<String, Map<String, Object>> courses = new TreeMap<>();
    for (var c : rows(r, "courses", 1, 200)) {
      keys(c, "id", "minutes", "prerequisites");
      String id = id(c, "id");
      if (courses.putIfAbsent(id, c) != null) throw new IllegalArgumentException("课程重复");
      integer(c, "minutes", 1, 100000);
      var deps = strings(c, "prerequisites", 0, 100);
      if (new HashSet<>(deps).size() != deps.size() || deps.contains(id))
        throw new IllegalArgumentException("先修重复或自引用");
    }
    for (var c : courses.values())
      if (!courses.keySet().containsAll(strings(c, "prerequisites", 0, 100)))
        throw new IllegalArgumentException("未知先修课程");
    var completed = strings(r, "completed", 0, 200);
    if (new HashSet<>(completed).size() != completed.size()
        || !courses.keySet().containsAll(completed))
      throw new IllegalArgumentException("完成记录重复或未知");
    Set<String> visited = new HashSet<>();
    while (visited.size() < courses.size()) {
      boolean progress = false;
      for (var e : courses.entrySet())
        if (!visited.contains(e.getKey())
            && visited.containsAll(strings(e.getValue(), "prerequisites", 0, 100))) {
          visited.add(e.getKey());
          progress = true;
        }
      if (!progress) throw new IllegalArgumentException("课程先修存在环路");
    }
    List<Map<String, Object>> out = new ArrayList<>();
    long remaining = 0;
    for (var e : courses.entrySet()) {
      String id = e.getKey();
      var c = e.getValue();
      var missing =
          strings(c, "prerequisites", 0, 100).stream().filter(k -> !completed.contains(k)).toList();
      boolean done = completed.contains(id);
      if (!done) remaining += integer(c, "minutes", 1, 100000);
      out.add(
          Map.of(
              "id",
              id,
              "state",
              done ? "COMPLETED" : missing.isEmpty() ? "READY" : "LOCKED",
              "missingPrerequisites",
              missing,
              "completionEvidenceInconsistent",
              done && !missing.isEmpty()));
    }
    return Map.of("remainingMinutes", remaining, "courses", out, "automaticCertification", false);
  }
}
