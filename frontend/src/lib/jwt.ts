export function decodeJwtSubject(token: string): string | null {
  try {
    const part = token.split('.')[1]
    if (!part) return null
    const json = atob(part.replace(/-/g, '+').replace(/_/g, '/'))
    const payload = JSON.parse(json) as { sub?: string }
    return payload.sub ?? null
  } catch {
    return null
  }
}
