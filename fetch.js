import { create } from "apisauce";

let api;

function createApiPromise(response) {
  return new Promise((resolve, reject) => {
    if (response.ok && response.data) {
      resolve(response.data);
    } else {
      reject(response.data || response.problem || "SOMETHING WENT WRONG");
    }
  });
}

function encodeParams(params) {
  return Object.entries(params)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
}

export function createApi(baseURL) {
  api = create({
    baseURL,
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
}

export async function get(url, params) {
  if (!api) throw new Error('API not initialized. Call createApi first.');

  const body = encodeParams(params);
  const response = await api.post(url, body);

  if (__DEV__) {
    console.log("Piano SDK - get - response:", response);
  }

  return createApiPromise(response);
}

export async function post(url, params, body) {
  if (!api) throw new Error('API not initialized. Call createApi first.');

  const queryString = encodeParams(params);
  const response = await api.post(`${url}?${queryString}`, body, {
    headers: {
      "Content-Type": "application/json",
    },
  });

  return createApiPromise(response);
}