import { defineStore } from "pinia"
import {
  CommunicationChannelPublicApiFrontendService,
  ContactApiConfig, ContactPublicApiFrontendService, IconHandlingApiConfig,
  IconHandlingPublicApiFrontendService, IconPublicApiFrontendService
} from "@contacts/frontend-api";
import {Temporal} from "@js-temporal/polyfill";
import Instant = Temporal.Instant;
import {findAllContacts} from "@/utils/ContactUtils.ts";

const apiContact = import.meta.env.VITE_CONTACT_API
const apiIconHandling = import.meta.env.VITE_ICON_HANDLING_API
const host = import.meta.env.VITE_SERVER_HOST_EXTERNAL + ":" + import.meta.env.VITE_API_PORT
const contactApiConfig= new ContactApiConfig(host, apiContact)
const iconHandlingApiConfig = new IconHandlingApiConfig(host, apiIconHandling)

export const myStore = defineStore("store", {

  state: () => ({
    contactApiService: new ContactPublicApiFrontendService(contactApiConfig),
    communicationChannelApiService: new CommunicationChannelPublicApiFrontendService(contactApiConfig),
    iconApiService: new IconPublicApiFrontendService(contactApiConfig),
    iconHandlingApiService: new IconHandlingPublicApiFrontendService(iconHandlingApiConfig),
    // count: 0,
    iconIdToPreviewUrl: new Map<bigint, string>,
    iconIdToFullQualityUrl: new Map<bigint, string>,
    contactIdToNextBirthdayInstant: new Map<bigint, Instant>,
    contactsWithBirthdayNow: new Set<bigint>,
    contactsWithBirthdaySoon: new Set<bigint>,
    contacts: findAllContacts(),
    hasContactJustBeenReplaced: false
  }),

  getters: {
    // double: (state) => state.count * 2
  }

})
