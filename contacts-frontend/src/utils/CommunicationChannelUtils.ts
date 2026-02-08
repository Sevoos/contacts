import type {CommunicationChannel} from "@/contact/CommunicationChannel.ts";

const getImgUrl = (name: string) =>
  new URL(`../assets/${name}`, import.meta.url).href

// export function getTypeName(type: EnumCommunicationChannel): string {
//   return type.name_1
// }

export function getChannelSrc(communicationChannel: CommunicationChannel): string {
  return getImgUrl(communicationChannel.type.toLowerCase() + ".svg")
}
