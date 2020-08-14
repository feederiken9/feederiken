# feederiken ![CI](https://github.com/feederiken/feederiken/workflows/CI/badge.svg)
Generate PGP keys with custom patterns in the fingerprint.

## Obtaining
Make sure you have Java installed.
Grab the self contained jar from the releases tab.

Alternatively, build from source using [mill](https://www.lihaoyi.com/mill).

## Usage
Use the `search` command to look for keys with a given prefix.
The default setting should take only a few seconds, so try that first.
The resulting keys are printed to the standard output in armored form so that e.g. they can be piped into `gpg`.

### Distributed operation
To use multiple machines in parallel, first, pick one of them to be the coordinator.
Then, you need to write a configuration file that describes your network based on the following template.
Your goal is that each node can securely (see security considerations) connect to the coordinator and vice versa.
```
coordinator.zio.actors.remoting {
  hostname = 127.0.0.1
  port = 8055
}
node0.zio.actors.remoting {
  hostname = 127.0.0.1
  port = 8056
}
```

Finally, start a `search` operation on the coordinator and pass it a copy of the configuration file using the `--config-file` flag.
It will print the address of the dispatcher actor in URL form.
Pass that address to the `node` command on all the other machines.
They will contact the dispatcher and contribute computing power to the search.
When the search is successful, every command will terminate shortly.

## Remarks
The fingerprint of a key is determined using a [hash function](https://en.wikipedia.org/wiki/Cryptographic_hash_function),
as a result, the best method to get a matching key is to generate a key, compute its fingerprint, and throw it away if it doesn't match until eventually we find one by chance.
As a consequence, there is no notion of progress that can be saved and restored on the way to finding a key, it's just a matter of chance and each attempt is just as likely to succeed as any other one.

### Security considerations
Keypairs generated by this application are conventional OpenPGP EdDSA signing keys and are drawn from a proper CSRNG.
The main trick that is used is to vary the generation timestamp to perturb the hash function, which doesnt affect the key proper.
To the best of my knowledge, neither this process nor any part of its implementation can introduce any specific weakness to the keys.

Private keys are not stored to disk. They may be transmitted unprotected over the network when running a distributed search.
If this is a concern, you should make sure that the underlying network routes offer confidentiality, possibly using a VPN link or an SSH tunnel.
