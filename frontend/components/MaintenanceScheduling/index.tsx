import styled from 'styled-components'

import { Calender, Back, Vacation } from '@navikt/ds-icons'
import { Systemtittel, Undertekst } from 'nav-frontend-typografi';



const MaintenanceSchedulingContainer = styled.div `
    display: flex;
    flex-direction: column;
`

const MaintenanceHeader = styled.div`
    margin-bottom: 2rem;
    display: flex;
    flex-direction: row;
    align-items: center;
    span {
        font-size: 2rem;
        margin-right: 10px;
    }
    margin-top: 10px;
    @media (min-width: 350px){
        margin-top: 0;
    }
`

const MaintenanceContent = styled.div``

const MaintenanceScheduling = () => {
    return (
        <MaintenanceSchedulingContainer>
            <MaintenanceHeader>
                <span><Calender /></span> <Systemtittel>Planlagt vedlikehold</Systemtittel>
            </MaintenanceHeader>
            <MaintenanceContent>
                {/* Two viewes based on whether theres maintenance scheduled or not */}
                Det er ingen planlagte vedlikehold.
            </MaintenanceContent>   
        </MaintenanceSchedulingContainer>
    )
}

export default MaintenanceScheduling