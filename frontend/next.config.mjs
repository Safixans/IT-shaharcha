/** @type {import('next').NextConfig} */
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:8080";

const nextConfig = {
  async rewrites() {
    // Proxy all /api/* calls to the API gateway so the browser sees a
    // same-origin request (no CORS) and tokens flow straight through.
    return [
      {
        source: "/api/:path*",
        destination: `${GATEWAY_URL}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
