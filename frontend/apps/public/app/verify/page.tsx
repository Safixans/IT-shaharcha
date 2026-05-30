import { Suspense } from "react";
import { VerifyForm } from "../../components/VerifyForm";

export const metadata = { title: "Verify your email" };

export default function VerifyPage() {
  return (
    <Suspense fallback={null}>
      <VerifyForm />
    </Suspense>
  );
}
