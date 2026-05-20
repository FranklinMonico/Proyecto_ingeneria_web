/**
 * En `npm run dev`, las rutas son relativas (/api/...) y Vite las reenvía al backend
 * (vite.config.ts → proxy a localhost:8081 por defecto).
 * En `npm run build` / `preview`, usa VITE_API_URL o http://localhost:8081 por defecto.
 */
const prodBase = (import.meta.env.VITE_API_URL ?? 'http://localhost:8081').replace(/\/$/, '')

export const API_BASE = import.meta.env.DEV ? '' : prodBase

/** WebSocket / SockJS: mismo origen en dev (proxy), en prod igual que API. */
export const WS_BASE = API_BASE
