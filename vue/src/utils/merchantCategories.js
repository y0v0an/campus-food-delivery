/**
 * 解析店铺「经营品类」：库内多为 JSON 数组字符串，也可能为逗号分隔或二次编码字符串
 */
export function parseMerchantCategories(raw) {
  if (raw == null || raw === '') return []
  if (Array.isArray(raw)) {
    return raw
      .map(String)
      .flatMap((s) => s.split(/[,，]/))
      .map((x) => x.trim())
      .filter(Boolean)
  }
  if (typeof raw === 'string') {
    let s = raw.trim()
    if (!s) return []
    try {
      let parsed = JSON.parse(s)
      if (Array.isArray(parsed)) {
        return parsed.map(String).map((x) => x.trim()).filter(Boolean)
      }
      if (typeof parsed === 'string') {
        try {
          const inner = JSON.parse(parsed)
          if (Array.isArray(inner)) {
            return inner.map(String).map((x) => x.trim()).filter(Boolean)
          }
        } catch {
          /* ignore */
        }
        return parsed.split(/[,，]/).map((x) => x.trim()).filter(Boolean)
      }
    } catch {
      /* 非 JSON */
    }
    return s.split(/[,，]/).map((x) => x.trim()).filter(Boolean)
  }
  return []
}

/** 写入接口/库表用的 JSON 字符串 */
export function categoriesToApiString(tags) {
  const arr = Array.isArray(tags) ? tags : parseMerchantCategories(tags)
  return JSON.stringify(arr.map(String).map((t) => t.trim()).filter(Boolean))
}
