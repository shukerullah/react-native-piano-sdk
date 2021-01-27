import { create } from "apisauce";

let api;

function MyPromise(response: { ok: boolean, data: {} }) {
  return new Promise((resolve, reject) => {
    if (response.ok && response.data) {
      resolve(response.data);
    }
    reject(response.data || response.problem || "SOMETHING WENT WRONG");
  });
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
  let body = [];
  Object.keys(params).forEach((key) => {
    body.push(`${key}=${params[key]}`);
  });
  body = body.join("&");
  const response = await api.post(url, body);
  return MyPromise(response);
}

export async function post(url, params, body) {
  let queryString = [];
  Object.keys(params).forEach((key) => {
    queryString.push(`${key}=${params[key]}`);
  });
  queryString = queryString.join("&");
  const response = await api.post(`${url}?${queryString}`, body, {
    headers: {
      "Content-Type": "application/json",
    },
  });
  return MyPromise(response);
}
