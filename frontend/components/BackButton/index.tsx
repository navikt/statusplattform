import { Back } from "@navikt/ds-icons"
import { Knapp } from "nav-frontend-knapper"

import styled from 'styled-components'

const KnappCustomized = styled(Knapp)`
    transition: 0.4s;
`

export const BackButton = () => {
    return (
        <KnappCustomized mini><Back/>Gå tilbake</KnappCustomized>
    )
}