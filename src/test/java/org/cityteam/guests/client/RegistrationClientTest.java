/*
 * Copyright 2020 CityTeam, craigmcc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cityteam.guests.client;

import org.cityteam.guests.action.Assign;
import org.cityteam.guests.action.Import;
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.types.FeatureType;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.cityteam.guests.model.types.PaymentType.$$;
import static org.cityteam.guests.model.types.PaymentType.AG;
import static org.cityteam.guests.model.types.PaymentType.CT;
import static org.cityteam.guests.model.types.PaymentType.MM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

public class RegistrationClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final FacilityClient facilityClient = new FacilityClient();
    private final RegistrationClient registrationClient = new RegistrationClient();

    // Lifecycle Methods -----------------------------------------------------

    @Before
    public void before() {
        if ((depopulateEnabled == null) || (TRUE == depopulateEnabled)) {
            depopulate();
        }
        if ((populateEnabled == null) || (TRUE == populateEnabled)) {
            populate();
        }
    }

    // Test Methods ----------------------------------------------------------

    // assign() tests

    @Test
    public void assignHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        LocalDate registrationDate = LocalDate.parse("2020-07-09");
        Registration registration = newRegistration
                (facility.getId(), 3, registrationDate);
        registration = registrationClient.insert(registration);

        Guest guest = facilityClient.findGuestsByNameExact
                (facility.getId(), "Barney", "Rubble");
        Assign assign = new Assign(
                "Barney in Oakland",
                guest.getId(),
                null,
                AG,
                null,
                null
        );

        registration = registrationClient.assign
                (registration.getId(), assign);
        assertThat(registration.getGuestId(), is(equalTo(guest.getId())));
        assertThat(registration.getPaymentType(), is(equalTo(AG)));

    }

    // deassign() tests

    @Test
    public void deassignHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Francisco");
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        Registration registration = newRegistration
                (facility.getId(), 3, registrationDate);
        registration = registrationClient.insert(registration);

        Guest guest = facilityClient.findGuestsByNameExact
                (facility.getId(), "Fred", "Flintstone");
        Assign assign = new Assign(
                "Fred in San Francisco",
                guest.getId(),
                null,
                CT,
                null,
                null
        );

        registration = registrationClient.assign
                (registration.getId(), assign);
        assertThat(registration.getGuestId(), is(equalTo(guest.getId())));
        assertThat(registration.getPaymentType(), is(equalTo(CT)));

        registration = registrationClient.deassign(registration.getId());
        assertThat(registration.getGuestId(), is(nullValue()));
        assertThat(registration.getPaymentType(), is(nullValue()));

    }

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Registration> registrations = registrationClient.findAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        for (Registration registration : registrations) {

            // Delete and verify we can no longer retrieve it
            registrationClient.delete(registration.getId());
            assertThrows(NotFound.class,
                    () -> registrationClient.find(registration.getId()));
        }

        assertThat(registrationClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> registrationClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Registration> registrations = registrationClient.findAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        for (Registration registration : registrations) {
            Registration found = registrationClient.find(registration.getId());
            assertThat(found.equals(registration), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> registrationClient.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Registration> registrations = registrationClient.findAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        String previousKey = null;
        for (Registration registration : registrations) {
            String thisKey = registration.getFacilityId() +
                    "|" + registration.getRegistrationDate() + "|" +
                    registration.getMatNumber();
            if (previousKey != null) {
                assertThat(thisKey, is(greaterThan(previousKey)));
            }
            previousKey = thisKey;
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");
        LocalDate registrationDate = LocalDate.parse("2020-07-08");
        Registration registration = newRegistration
                (facility.getId(), 1, registrationDate);
        Registration inserted = registrationClient.insert(registration);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(inserted.getFacilityId(), is(equalTo(facility.getId())));
        assertThat(inserted.getFeatures(), is(nullValue()));
        assertThat(inserted.getGuestId(), is(nullValue()));
        assertThat(inserted.getMatNumber(), is(equalTo(registration.getMatNumber())));
        assertThat(inserted.getRegistrationDate(), is(equalTo(registrationDate)));

    }

    // TODO - insertBadRequest() tests

    //  TODO - insertNotUnique() tests

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");
        LocalDate registrationDate = LocalDate.parse("2020-07-07");
        Registration registration = newRegistration
                (facility.getId(), 2, registrationDate);
        Registration inserted = registrationClient.insert(registration);

        assertThrows(InternalServerError.class,
                () -> registrationClient.update(0L, inserted));

    }

    // Support Methods -------------------------------------------------------

    private Registration newRegistration
            (Long facilityId, Integer matNumber, LocalDate registrationDate) {
        return new Registration(
                facilityId,
                null,
                matNumber,
                registrationDate
        );
    }

}
