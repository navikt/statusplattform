import Link from 'next/link'

import Layout from 'components/Layout'
import styled from 'styled-components'
import { Calender, Back } from '@navikt/ds-icons'
import { Knapp } from 'nav-frontend-knapper'
import NavInfoCircle from 'components/NavInfoCircle'
import Alertstripe from 'nav-frontend-alertstriper'
import { BackButton } from 'components/BackButton'

const IncidentsContainer = styled.div`
    margin: 20px 0;
    width: 100%;
`
const CenterContent = styled.div`
    margin: 0 auto;
    max-width: 1100px;
    padding: 1rem 1rem;
    display: flex;
    flex-direction: column;
    @media(min-width:450px){
        padding: 1rem 3rem;
    }
`
const KnappCustomized = styled(Knapp)`transition: 0.4s;`

const SectionContainer = styled.div`
    display: flex;
    justify-content: center;
    flex-direction: column;
    align-items: flex-start;
    margin: 10px 0;
`
const IconWrapper = styled.div`
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    *:first-child{
        margin-right: 20px;
    }
`

const IncidentsWrapper = styled.div`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    
`
const ExistsIncidents = styled.div``

const WhiteBackgroundContainer = styled.div`
    background-color: var(--navBakgrunn);
    width: 100%;
    height: 100%;
`

const Incidents = () => {
    const numberOfIncidents: number = 0
    return (
        <Layout>
            <IncidentsContainer>
                <CenterContent>
                    <Link href="/"><span><BackButton/></span></Link>


                    <SectionContainer>
                        <IconWrapper>
                            <Calender style={{"fontSize": "3rem"}} aria-label="Kalender ikon" role="img" focusable="false"/>
                            <div>
                                <h3>Hendelser</h3>
                                <span>Siste 48 timer</span>
                            </div>
                        </IconWrapper>
                    </SectionContainer>

                </CenterContent>


                {/* TODO: Handle Incidents within this wrapper.  */}
                <WhiteBackgroundContainer>
                    <CenterContent>
                        <IncidentsWrapper>
                            <CenterContent>
                                {numberOfIncidents > 0 ? (
                                        <ExistsIncidents>
                                            <NavInfoCircle topText={"Antall hendelser"} centerText={numberOfIncidents} bottomText="Siste 48 timene" />
                                        </ExistsIncidents>
                                    ) : (
                                        <CenterContent>
                                            <Alertstripe type="suksess">Ingen hendelser registrert!</Alertstripe>
                                        </CenterContent>
                                    )
                            }
                            </CenterContent>
                        </IncidentsWrapper>
                    </CenterContent>
                </WhiteBackgroundContainer>


                <CenterContent>
                    <SectionContainer>
                        <IconWrapper>
                            <Calender style={{"fontSize": "3rem"}} aria-label="Kalender ikon" role="img" focusable="false"/>
                            <div>
                                <h3>Hendelser</h3>
                                <span>Siste 90 dagene</span>
                            </div>
                        </IconWrapper>
                    </SectionContainer>
                </CenterContent>
                


                <CenterContent>
                    <IncidentsWrapper>
                        BANNER og OPPSUMMERING siste 90 dager
                    </IncidentsWrapper>
                </CenterContent>



            </IncidentsContainer>
        </Layout>
    )
}

export default Incidents