const API_ORIGIN = 'http://localhost:8080'

/** 解析评价里存储的 images 字段（JSON 数组、逗号分隔、单路径等） */
export function parseReviewImages(images) {
  if (images == null || images === '') return []
  if (Array.isArray(images)) return images.map((u) => String(u).trim()).filter(Boolean)
  if (typeof images === 'string') {
    const t = images.trim()
    if (!t) return []
    if (t.startsWith('[') || t.startsWith('{')) {
      try {
        const p = JSON.parse(t)
        if (Array.isArray(p)) return p.map((u) => String(u).trim()).filter(Boolean)
        return []
      } catch {
        return []
      }
    }
    if (t.includes(',')) {
      return t
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
    }
    if (t.startsWith('/') || t.startsWith('http') || t.startsWith('img/')) {
      return [t]
    }
    return []
  }
  return []
}

/** 将存储的相对路径转为可访问的完整图片 URL（静态资源由后端 8080 提供） */
export function getImageUrl(url) {
  if (url == null || url === '') return ''
  const s = String(url).trim()
  if (!s) return ''
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  if (s.startsWith('/img/')) return `${API_ORIGIN}${s}`
  if (s.startsWith('img/')) return `${API_ORIGIN}/${s}`
  return s
}
