#!/usr/bin/env python3
"""
Generates a sample .crt certificate similar to the one provided by the
Ministry of Finance's free DTE signing tool (El Salvador).

El .crt real es un XML (CertificadoMH) donde:
  - <nit> identifies the signer -> the file must be named {nit}.crt
  - <privateKey><embodied> is the RSA private key in DER PKCS#8, Base64-Std
  - <privateKey><clave> is hex(SHA-512(private key password))

The signer verifies: hex(SHA-512(passwordPri)) == privateKey.clave

Usage:
  python3 scripts/generate-test-certificate.py --nit 061400000001 --password test123
   python3 scripts/generate-test-certificate.py --nit 061400000001 --password test123 --dir ./uploads/certs --bits 2048

Requires: pip install cryptography
"""
import argparse
import base64
import hashlib
import os
import sys

try:
    from cryptography.hazmat.primitives.asymmetric import rsa
    from cryptography.hazmat.primitives import serialization
except ImportError:
    sys.exit("Missing 'cryptography' package: python3 -m pip install cryptography")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate a sample MH-style .crt certificate.")
    parser.add_argument("--nit", required=True, help="NIT without dashes, e.g. 061400000001")
    parser.add_argument("--password", required=True, help="Private key password (used as passwordPri)")
    parser.add_argument("--dir", default="./uploads/certs", help="Output directory (must match CERTIFICATE_DIRECTORY)")
    parser.add_argument("--bits", type=int, default=2048, help="RSA key size (default: 2048)")
    args = parser.parse_args()

    key = rsa.generate_private_key(public_exponent=65537, key_size=args.bits)
    der = key.private_bytes(
        serialization.Encoding.DER,
        serialization.PrivateFormat.PKCS8,
        serialization.NoEncryption(),
    )
    embodied = base64.b64encode(der).decode("ascii")
    password_hash = hashlib.sha512(args.password.encode("utf-8")).hexdigest()

    xml = (
        "<CertificadoMH>"
        "<_id>simulado</_id>"
        "<activo>1</activo>"
        f"<nit>{args.nit}</nit>"
        "<privateKey>"
        "<algorithm>RSA</algorithm>"
        f"<clave>{password_hash}</clave>"
        f"<embodied>{embodied}</embodied>"
        "<format>PKCS8</format>"
        "<keyType>private</keyType>"
        "</privateKey>"
        "</CertificadoMH>"
    )

    os.makedirs(args.dir, exist_ok=True)
    path = os.path.join(args.dir, f"{args.nit}.crt")
    with open(path, "w", encoding="utf-8") as f:
        f.write(xml)

    print(f"Sample certificate: {path}")
    print(f"  nit         : {args.nit}")
    print(f"  passwordPri : {args.password}  (SHA-512 hash: {password_hash[:16]}...)")
    print("NOTE: the signer uses lenient parsing; real MH .crt files contain more")
    print("XML fields, but only nit and privateKey.{clave,embodied} are used.")


if __name__ == "__main__":
    main()
