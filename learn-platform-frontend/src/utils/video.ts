/**
 * 视频链接解析工具
 */
export interface VideoInfo {
  type: 'FILE' | 'BILIBILI' | 'YOUTUBE' | 'UNKNOWN'
  embedUrl: string
}

export const getVideoInfo = (url: string): VideoInfo => {
  if (!url) return { type: 'UNKNOWN', embedUrl: '' }

  // 1. Check for Bilibili
  // Patterns: 
  // https://www.bilibili.com/video/BV15q9jB6Eei/
  // https://b23.tv/BV15q9jB6Eei
  const bvidMatch = url.match(/BV[a-zA-Z0-9]+/) || url.match(/av\d+/)
  if (url.includes('bilibili.com') || url.includes('b23.tv')) {
    if (bvidMatch) {
      const bvid = bvidMatch[0]
      return {
        type: 'BILIBILI',
        embedUrl: `//player.bilibili.com/player.html?${bvid.startsWith('BV') ? `bvid=${bvid}` : `aid=${bvid.replace('av', '')}`}&page=1&high_quality=1&danmaku=0`
      }
    }
  }

  // 2. Check for YouTube
  // Patterns:
  // https://www.youtube.com/watch?v=dQw4w9WgXcQ
  // https://youtu.be/dQw4w9WgXcQ
  const ytMatch = url.match(/(?:v=|\/be\/|embed\/)([^&?#/]+)/)
  if (url.includes('youtube.com') || url.includes('youtu.be')) {
    if (ytMatch) {
      return {
        type: 'YOUTUBE',
        embedUrl: `https://www.youtube.com/embed/${ytMatch[1]}`
      }
    }
  }

  // 3. Check for Direct Video Files
  const isVideoFile = /\.(mp4|webm|ogg|m4v|mov)(?:\?|$)/i.test(url)
  if (isVideoFile) {
    return { type: 'FILE', embedUrl: url }
  }

  // Default to unknown if no match found
  return { type: 'UNKNOWN', embedUrl: url }
}
