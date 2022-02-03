package io.origintrail.dkg.client.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NQuadTest {

    @Test
    void toString_withNoGraphProperty_toStringReturnsTripleWithNoGraph() {
        // given
        NQuad nQuad = NQuad.builder("<did:dkg:5285f5da78edeaa9a7cb853ebd85121fae7d93191490d182490464668f2d21cc>", "<http://schema.org/hasKeyword>", "aKeyword").build();

        // when
        String nQuadString = nQuad.toString();

        // then
        assertThat(nQuadString).isEqualTo("<did:dkg:5285f5da78edeaa9a7cb853ebd85121fae7d93191490d182490464668f2d21cc> <http://schema.org/hasKeyword> aKeyword .");
    }

    @Test
    void toString_withGraphProperty_toStringReturnsTripleWithGraph() {
        // given
        NQuad nQuad = NQuad
                .builder("<did:dkg:5285f5da78edeaa9a7cb853ebd85121fae7d93191490d182490464668f2d21cc>", "<http://schema.org/hasKeyword>", "aKeyword")
                .graph("graphName")
                .build();

        // when
        String nQuadString = nQuad.toString();

        // then
        assertThat(nQuadString).isEqualTo("<did:dkg:5285f5da78edeaa9a7cb853ebd85121fae7d93191490d182490464668f2d21cc> <http://schema.org/hasKeyword> aKeyword graphName .");
    }
}