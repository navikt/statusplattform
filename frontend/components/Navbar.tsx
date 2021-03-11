import { useRouter } from 'next/router'
import Link from 'next/link'

import styled from 'styled-components'

import Lenke from 'nav-frontend-lenker';
import { Normaltekst, } from "nav-frontend-typografi";

const Nav = styled.div `
	height: 3.75rem;
	width: 100%;
    background-color: white;
	display: flex;
	align-items: center;
	> ul {
		padding-left: 0;
		margin: 0 auto;
	}
	> ul > li {
		display: inline-block;
		outline: none;
	}
	a {
		height: calc(100% + 1px);
		display: inline-block;
		text-decoration: none;
		color: black;
		&active {
			border-bottom: var(--navBla) 3px solid;
		}
	}
	@media (min-width: 350px){
		height: 2.75rem;
		> ul {
			margin: 0 auto;
		}
	}
	@media (min-width: 468px) {
		> ul {
			margin: inherit;
			padding-left: 112px;
		}
	}
`

const LinkWrapper = styled.div`
	height: 100%;
    margin: 0 1rem;
    border-bottom: transparent 3px solid;
	display: flex;
    align-items: center;
	justify-content: center;
`;
const LenkeCustomized = styled(Lenke)`
	.active {
		border-bottom-color: var(--fokusFarge) !important;
		> p {
			font-weight: bold !important;
			color: black !important;
		}
	}
`;
const LenkeInner = styled.div`
	border-bottom: transparent 3px solid;
	:hover {
		border-bottom: var(--navBla) 3px solid;
		color: var(--navBla);
	}
`;

const NormalTekstCustomized = styled(Normaltekst)`
	font-size: 1rem;
	line-height: 1.375rem;
`;


export default function Navbar() {
	const router = useRouter()

	return (
		<Nav>
			<ul role="tablist">
				<li role="tab">
					<LinkWrapper>
						<Link href="/">
							<LenkeCustomized>
								<LenkeInner className={`${router.pathname === "/" ? "active" : ""}`}>
									<NormalTekstCustomized>Privatperson</NormalTekstCustomized>
								</LenkeInner>
							</LenkeCustomized>
						</Link>
					</LinkWrapper>
				</li>
				<li role="tab">
					<LinkWrapper>
						<Link href="/arbeidsgiver">
							<LenkeCustomized>
								<LenkeInner className={`${router.pathname === "/arbeidsgiver" ? "active" : ""}`}>
									<NormalTekstCustomized>Arbeidsgiver</NormalTekstCustomized>
								</LenkeInner>
							</LenkeCustomized>
						</Link>
					</LinkWrapper>
				</li>
			</ul>
		</Nav>
	)
}