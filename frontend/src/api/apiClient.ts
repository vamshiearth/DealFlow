import { clearToken, getToken } from '../auth/tokenStorage'

export async function apiFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
): Promise<Response> {
  const token = getToken()
  const headers = new Headers(init.headers)

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(input, {
    ...init,
    headers,
  })

  if (response.status === 401) {
    clearToken()
  }

  return response
}
